package ru.yandex.practicum.market.order;

import ru.yandex.practicum.market.cart.CartRepository;
import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.item.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderRepositoryTest {

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	OrderItemRepository orderItemRepository;

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	CartRepository cartRepository;

	@BeforeEach
	void cleanDb() {
		orderItemRepository.deleteAll().block();
		orderRepository.deleteAll().block();
		cartRepository.deleteAll().block();
		itemRepository.deleteAll().block();
	}

	@Test
	void save_persistsOrderWithLineItems() {
		Item product = saveProduct("Product_OrderRepo_One", 100L);
		Order order = orderRepository.save(newOrderForUser()).block();

		OrderItem orderItem = new OrderItem();
		orderItem.setOrderId(order.getId());
		orderItem.setItemId(product.getId());
		orderItem.setCount(2);
		orderItemRepository.save(orderItem).block();

		Order found = orderRepository.findById(order.getId()).block();
		List<OrderItem> lines = orderItemRepository.findByOrderId(order.getId()).collectList().block();

		assertThat(found).isNotNull();
		assertThat(found.getId()).isNotNull();
		assertThat(lines).hasSize(1);
		assertThat(lines.get(0).getCount()).isEqualTo(2);
		assertThat(lines.get(0).getItemId()).isEqualTo(product.getId());
	}

	@Test
	void findById_emptyOrder_returnsOrderWithNoItems() {
		Order saved = orderRepository.save(newOrderForUser()).block();

		List<OrderItem> lines = orderItemRepository.findByOrderId(saved.getId()).collectList().block();

		assertThat(lines).isEmpty();
	}

	@Test
	void findAll_returnsAllPersistedOrders() {
		orderRepository.save(newOrderForUser()).block();
		Item p = saveProduct("Product_OrderRepo_Two", 1L);
		Order order = orderRepository.save(newOrderForUser()).block();
		OrderItem orderItem = new OrderItem();
		orderItem.setOrderId(order.getId());
		orderItem.setItemId(p.getId());
		orderItem.setCount(1);
		orderItemRepository.save(orderItem).block();

		List<Order> all = orderRepository.findAll().collectList().block();

		assertThat(all).hasSize(2);
	}

	@Test
	void save_multipleOrderItemsOnSameOrder() {
		Item productA = saveProduct("Product_OrderRepo_A", 10L);
		Item productB = saveProduct("Product_OrderRepo_B", 20L);
		Order order = orderRepository.save(newOrderForUser()).block();

		OrderItem first = new OrderItem();
		first.setOrderId(order.getId());
		first.setItemId(productA.getId());
		first.setCount(1);
		orderItemRepository.save(first).block();

		OrderItem second = new OrderItem();
		second.setOrderId(order.getId());
		second.setItemId(productB.getId());
		second.setCount(3);
		orderItemRepository.save(second).block();

		List<OrderItem> lines = orderItemRepository.findByOrderId(order.getId()).collectList().block();
		assertThat(lines).hasSize(2);
	}

	private Item saveProduct(String title, long price) {
		Item item = new Item();
		item.setTitle(title);
		item.setPrice(price);
		return itemRepository.save(item).block();
	}

	private Order newOrderForUser() {
		Order order = new Order();
		order.setUserId(1L);
		return order;
	}
}
