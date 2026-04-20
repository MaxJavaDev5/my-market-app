package ru.yandex.practicum.market.order;

import ru.yandex.practicum.market.cart.CartRepository;
import ru.yandex.practicum.market.cart.CartService;
import ru.yandex.practicum.market.cart.CartAction;
import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.item.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OrderServiceTest {

	@Autowired
	OrderService orderService;

	@Autowired
	CartService cartService;

	@Autowired
	CartRepository cartRepository;

	@Autowired
	ItemRepository itemRepository;

	@Autowired
	OrderItemRepository orderItemRepository;

	@Autowired
	OrderRepository orderRepository;

	@BeforeEach
	void cleanDb() {
		orderItemRepository.deleteAll().block();
		orderRepository.deleteAll().block();
		cartRepository.deleteAll().block();
		itemRepository.deleteAll().block();
	}

	@Test
	void order_createOrder_emptyCart_savesOrderWithNoLineItems() {
		int ordersBefore = orderService.findAll().collectList().block().size();

		Order created = orderService.createOrder().block();

		assertThat(created.getId()).isNotNull();
		assertThat(created.getItems()).isEmpty();
		assertThat(orderService.findAll().collectList().block()).hasSize(ordersBefore + 1);
		assertThat(cartRepository.findAll().collectList().block()).isEmpty();
	}

	@Test
	void order_createOrder_copiesCartLineItemsAndClearsCart() {
		Item product = saveProduct("Product_Order_Cart", 100L);
		cartService.updateCart(product.getId(), CartAction.PLUS).block();
		cartService.updateCart(product.getId(), CartAction.PLUS).block();

		Order created = orderService.createOrder().block();

		assertThat(cartRepository.findAll().collectList().block()).isEmpty();
		assertThat(created.getItems()).hasSize(1);
		assertThat(created.getItems().get(0).getCount()).isEqualTo(2);
		assertThat(created.getItems().get(0).getItem().getId()).isEqualTo(product.getId());
	}

	@Test
	void order_findById_returnsPersistedOrder() {
		Item product = saveProduct("Product_Order_FindById", 50L);
		cartService.updateCart(product.getId(), CartAction.PLUS).block();
		Order created = orderService.createOrder().block();

		Order found = orderService.findById(created.getId()).block();

		assertThat(found.getId()).isEqualTo(created.getId());
		assertThat(found.getItems()).hasSize(1);
		assertThat(found.getItems().get(0).getCount()).isEqualTo(1);
	}

	@Test
	void order_findById_notFound_throws() {
		RuntimeException ex = assertThrows(RuntimeException.class,
				() -> orderService.findById(999_999L).block());
		assertThat(ex.getMessage()).contains("Order not found");
	}

	@Test
	void order_findAll_setsTotalSumPerOrder() {
		Item productOne = saveProduct("Product_Order_Sum_One", 10L);
		Item productTwo = saveProduct("Product_Order_Sum_Two", 5L);

		cartService.updateCart(productOne.getId(), CartAction.PLUS).block();
		cartService.updateCart(productOne.getId(), CartAction.PLUS).block();
		orderService.createOrder().block();

		cartService.updateCart(productTwo.getId(), CartAction.PLUS).block();
		cartService.updateCart(productTwo.getId(), CartAction.PLUS).block();
		cartService.updateCart(productTwo.getId(), CartAction.PLUS).block();
		orderService.createOrder().block();

		List<Order> orders = orderService.findAll().collectList().block();

		assertThat(orders).hasSize(2);
		assertThat(orders.stream().map(Order::getTotalSum).collect(Collectors.toSet()))
				.containsExactlyInAnyOrder(20L, 15L);
	}

	private Item saveProduct(String title, long price) {
		Item item = new Item();
		item.setTitle(title);
		item.setPrice(price);
		return itemRepository.save(item).block();
	}
}
