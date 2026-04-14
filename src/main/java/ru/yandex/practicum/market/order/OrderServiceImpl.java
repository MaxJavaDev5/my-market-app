package ru.yandex.practicum.market.order;

import ru.yandex.practicum.market.cart.CartRepository;
import ru.yandex.practicum.market.cart.CartService;
import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.item.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

	private final CartService cartService;
	private final CartRepository cartRepository;
	private final ItemRepository itemRepository;
	private final OrderRepository orderRepository;

	public OrderServiceImpl(OrderRepository orderRepository,
			CartService cartService,
			CartRepository cartRepository,
			ItemRepository itemRepository)
	{
		this.orderRepository = orderRepository;
		this.cartService = cartService;
		this.cartRepository = cartRepository;
		this.itemRepository = itemRepository;
	}

	@Override
	@Transactional
	public Order createOrder()
	{
		List<Item> cartItems = cartService.getCartItems();
		Order order = new Order();

		List<OrderItem> orderItems = cartItems.stream()
				.map(cartItem -> {
					OrderItem orderItem = new OrderItem();
					orderItem.setOrder(order);
					orderItem.setItem(itemRepository.getReferenceById(cartItem.getId()));
					orderItem.setCount(cartItem.getCount());
					return orderItem;
				})
				.collect(Collectors.toList());
		order.setItems(orderItems);

		Order saved = orderRepository.save(order);
		cartRepository.deleteAll();
		return saved;
	}

	@Override
	@Transactional
	public Order findById(Long id)
	{
		return orderRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Order not found: " + id));
	}

	@Override
	@Transactional
	public List<Order> findAll()
	{
		List<Order> orders = orderRepository.findAll();

		orders.forEach(order -> {
			long total = order.getItems().stream()
					.mapToLong(orderItem -> orderItem.getItem().getPrice() * orderItem.getCount())
					.sum();
			order.setTotalSum(total);
		});

		return orders;
	}
}
