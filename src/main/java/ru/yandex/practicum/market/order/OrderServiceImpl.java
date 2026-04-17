package ru.yandex.practicum.market.order;

import ru.yandex.practicum.market.cart.CartRepository;
import ru.yandex.practicum.market.cart.CartService;
import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.item.ItemRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

	private final CartService cartService;
	private final CartRepository cartRepository;
	private final ItemRepository itemRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

	public OrderServiceImpl(OrderRepository orderRepository,
			OrderItemRepository orderItemRepository,
			CartService cartService,
			CartRepository cartRepository,
			ItemRepository itemRepository)
	{
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.cartService = cartService;
		this.cartRepository = cartRepository;
		this.itemRepository = itemRepository;
	}

	@Override
	public Mono<Order> createOrder() {
		return cartService.getCartItems()
				.collectList()
				.flatMap(this::saveOrderAndClearCart);
	}

	@Override
	public Mono<Order> findById(Long id)
	{
		return loadOrderWithItems(id);
	}

	@Override
	public Flux<Order> findAll() {
		return orderRepository.findAll()
				.flatMap(order -> loadOrderWithItems(order.getId()));
	}

	private Mono<Order> loadOrderWithItems(Long id) {
		return orderRepository.findById(id)
				.switchIfEmpty(Mono.error(new RuntimeException("Order not found: " + id)))
				.flatMap(order -> orderItemRepository.findByOrderId(order.getId())
						.flatMap(line -> itemRepository.findById(line.getItemId())
								.map(item -> {
									line.setItem(item);
									return line;
								}))
						.collectList()
						.map(lines -> {
							order.setItems(lines);
							long total = lines.stream()
									.mapToLong(l -> l.getItem().getPrice() * l.getCount())
									.sum();
							order.setTotalSum(total);
							return order;
						}));
	}

	private Mono<Order> saveOrderAndClearCart(List<Item> cartItems) {
		Order draft = new Order();
		return orderRepository.save(draft)
				.flatMap(order -> {
					if (cartItems.isEmpty()) {
						return cartRepository.deleteAll().thenReturn(order.getId());
					}
					return Flux.fromIterable(cartItems)
							.flatMap(ci -> {
								OrderItem line = new OrderItem();
								line.setOrderId(order.getId());
								line.setItemId(ci.getId());
								line.setCount(ci.getCount());
								return orderItemRepository.save(line);
							})
							.then(cartRepository.deleteAll())
							.thenReturn(order.getId());
				})
				.flatMap(this::loadOrderWithItems);
	}
}
