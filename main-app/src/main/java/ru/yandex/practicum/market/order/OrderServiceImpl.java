package ru.yandex.practicum.market.order;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import ru.yandex.practicum.market.cart.CartRepository;
import ru.yandex.practicum.market.cart.CartService;
import ru.yandex.practicum.market.auth.UserRepository;
import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.item.ItemRepository;
import ru.yandex.practicum.market.payment.PaymentGateway;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

	private final CartService cartService;
	private final CartRepository cartRepository;
	private final ItemRepository itemRepository;
	private final PaymentGateway paymentGateway;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final UserRepository userRepository;

	public OrderServiceImpl(OrderRepository orderRepository,
			OrderItemRepository orderItemRepository,
			CartService cartService,
			CartRepository cartRepository,
			ItemRepository itemRepository,
			PaymentGateway paymentGateway,
			UserRepository userRepository)
	{
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.cartService = cartService;
		this.cartRepository = cartRepository;
		this.itemRepository = itemRepository;
		this.paymentGateway = paymentGateway;
		this.userRepository = userRepository;
	}

	@Override
	public Mono<Order> createOrder()
	{
		return getCurrentUserId()
				.flatMap(userId -> cartService.getCartItems()
						.collectList()
						.flatMap(cartItems -> chargeAndSaveOrder(userId, cartItems)));
	}

	@Override
	public Mono<Order> findById(Long id)
	{
		return getCurrentUserId()
				.flatMap(userId -> loadOrderWithItems(id, userId));
	}

	@Override
	public Flux<Order> findAll()
	{
		return getCurrentUserId()
				.flatMapMany(orderRepository::findAllByUserId)
				.flatMap(order ->
						loadOrderWithItems(order.getId(), order.getUserId()));
	}

	private Mono<Order> chargeAndSaveOrder(Long userId, List<Item> cartItems)
	{
		long total = calcTotal(cartItems);

		return paymentGateway.charge(total, null)
				.then(saveOrderAndClearCart(userId, cartItems));
	}

	private Mono<Order> loadOrderWithItems(Long id, Long userId)
	{
		return orderRepository.findByIdAndUserId(id, userId)
				.switchIfEmpty(Mono.error(new Exception("Order not found: " + id + " for user " + userId)))
				.flatMap(this::fillOrderItemsAndTotal);
	}

	private Mono<Order> fillOrderItemsAndTotal(Order order)
	{
		return loadOrderItems(order.getId())
				.collectList()
				.map(lines ->
				{
					order.setItems(lines);
					order.setTotalSum(calcTotalFromOrderItems(lines));
					return order;
				});
	}

	private Flux<OrderItem> loadOrderItems(Long orderId)
	{
		return orderItemRepository.findByOrderId(orderId)
				.flatMap(this::attachItemToLine);
	}

	private Mono<OrderItem> attachItemToLine(OrderItem line)
	{
		return itemRepository
				.findById(line.getItemId())
				.map(item -> {
					line.setItem(item);
					return line;
				});
	}

	private Mono<Order> saveOrderAndClearCart(Long userId, List<Item> cartItems)
	{
		Order draft = new Order();
		draft.setUserId(userId);
		return orderRepository
				.save(draft)
				.flatMap(order -> saveOrderItems(order.getId(), cartItems).thenReturn(order.getId()))
				.flatMap(orderId -> cartRepository.deleteAllByUserId(userId).thenReturn(orderId))
				.flatMap(orderId -> loadOrderWithItems(orderId, userId));
	}

	private Mono<Void> saveOrderItems(Long orderId, List<Item> cartItems) {
		if (cartItems.isEmpty()) {
			return Mono.empty();
		}
		return Flux.fromIterable(cartItems)
				.flatMap(item -> orderItemRepository.save(createOrderItem(orderId, item)))
				.then();
	}

	private OrderItem createOrderItem(Long orderId, Item item) {
		OrderItem line = new OrderItem();
		line.setOrderId(orderId);
		line.setItemId(item.getId());
		line.setCount(item.getCount());
		return line;
	}

	private long calcTotal(List<Item> items) {
		return items.stream()
				.mapToLong(item -> item.getPrice() * item.getCount())
				.sum();
	}

	private long calcTotalFromOrderItems(List<OrderItem> lines) {
		return lines.stream()
				.mapToLong(line -> line.getItem().getPrice() * line.getCount())
				.sum();
	}

	private Mono<Long> getCurrentUserId() {
		return ReactiveSecurityContextHolder.getContext()
				.map(securityContext -> securityContext.getAuthentication())
				.map(Authentication::getName)
				.flatMap(userRepository::findByUsername)
				.map(user -> user.getId())
				.defaultIfEmpty(1L);
	}
}
