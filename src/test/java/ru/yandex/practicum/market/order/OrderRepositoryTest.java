package ru.yandex.practicum.market.order;

import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.item.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

	@Autowired
	OrderRepository orderRepository;

	@Autowired
	ItemRepository itemRepository;

	@Test
	void save_persistsOrderWithLineItems() {
		Item product = saveProduct("Product_OrderRepo_One", 100L);
		Order order = new Order();
		OrderItem orderItem = new OrderItem();
		orderItem.setOrder(order);
		orderItem.setItem(product);
		orderItem.setCount(2);
		order.getItems().add(orderItem);

		Order saved = orderRepository.save(order);

		assertThat(saved.getId()).isNotNull();
		Order found = orderRepository.findById(saved.getId()).orElseThrow();
		assertThat(found.getItems()).hasSize(1);
		assertThat(found.getItems().get(0).getCount()).isEqualTo(2);
		assertThat(found.getItems().get(0).getItem().getId()).isEqualTo(product.getId());
		assertThat(found.getItems().get(0).getItem().getPrice()).isEqualTo(100L);
	}

	@Test
	void findById_emptyOrder_returnsOrderWithNoItems() {
		Order saved = orderRepository.save(new Order());

		Order found = orderRepository.findById(saved.getId()).orElseThrow();

		assertThat(found.getItems()).isEmpty();
	}

	@Test
	void findAll_returnsAllPersistedOrders() {
		orderRepository.save(new Order());
		Item p = saveProduct("Product_OrderRepo_Two", 1L);
		Order order = new Order();
		OrderItem orderItem = new OrderItem();
		orderItem.setOrder(order);
		orderItem.setItem(p);
		orderItem.setCount(1);
		order.getItems().add(orderItem);
		orderRepository.save(order);

		List<Order> all = orderRepository.findAll();

		assertThat(all).hasSize(2);
	}

	@Test
	void save_multipleOrderItemsOnSameOrder() {
		Item productA = saveProduct("Product_OrderRepo_A", 10L);
		Item productB = saveProduct("Product_OrderRepo_B", 20L);
		Order order = new Order();
		OrderItem first = new OrderItem();
		first.setOrder(order);
		first.setItem(productA);
		first.setCount(1);
		OrderItem second = new OrderItem();
		second.setOrder(order);
		second.setItem(productB);
		second.setCount(3);
		order.getItems().add(first);
		order.getItems().add(second);

		Order saved = orderRepository.save(order);

		Order found = orderRepository.findById(saved.getId()).orElseThrow();
		assertThat(found.getItems()).hasSize(2);
	}

	private Item saveProduct(String title, long price) {
		Item item = new Item();
		item.setTitle(title);
		item.setPrice(price);
		return itemRepository.save(item);
	}
}
