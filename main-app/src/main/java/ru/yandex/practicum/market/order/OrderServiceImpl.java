package ru.yandex.practicum.market.order;

import ru.yandex.practicum.market.cart.CartRepository;
import ru.yandex.practicum.market.cart.CartService;
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

	public OrderServiceImpl(OrderRepository orderRepository,
			OrderItemRepository orderItemRepository,
			CartService cartService,
			CartRepository cartRepository,
			ItemRepository itemRepository,
			PaymentGateway paymentGateway)
	{
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.cartService = cartService;
		this.cartRepository = cartRepository;
		this.itemRepository = itemRepository;
		this.paymentGateway = paymentGateway;
	}

	@Override
	public Mono<Order> createOrder()
	{
		return cartService.getCartItems()
				.collectList()
				.flatMap(this::chargeAndSaveOrder);
	}

	@Override
	public Mono<Order> findById(Long id)
	{
		return loadOrderWithItems(id);
	}

	@Override
	public Flux<Order> findAll()
	{
		return orderRepository.findAll()
				.flatMap(order ->
						loadOrderWithItems(order.getId()));
	}

	private Mono<Order> chargeAndSaveOrder(List<Item> cartItems)
	{
		long total = calcTotal(cartItems);

		return paymentGateway.charge(total, null)
				.then(saveOrderAndClearCart(cartItems));
	}

	private Mono<Order> loadOrderWithItems(Long id)
	{
		return orderRepository.findById(id)
				.switchIfEmpty(Mono.error(new Exception("Order not found: " + id)))
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

	private Mono<Order> saveOrderAndClearCart(List<Item> cartItems)
	{
		Order draft = new Order();
		return orderRepository
				.save(draft)
				.flatMap(order -> saveOrderItems(order.getId(), cartItems).thenReturn(order.getId()))
				.flatMap(orderId -> cartRepository.deleteAll().thenReturn(orderId))
				.flatMap(this::loadOrderWithItems);
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
}
