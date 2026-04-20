package ru.yandex.practicum.market.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.cart.CartAction;
import ru.yandex.practicum.market.cart.CartRepository;
import ru.yandex.practicum.market.cart.CartService;
import ru.yandex.practicum.market.item.Item;
import ru.yandex.practicum.market.item.ItemRepository;
import ru.yandex.practicum.market.payment.PaymentGateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
class OrderPaymentIntegrationTest {

	@Autowired
	private WebTestClient webTestClient;
	@Autowired
	private CartService cartService;
	@Autowired
	private CartRepository cartRepository;
	@Autowired
	private ItemRepository itemRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private OrderItemRepository orderItemRepository;

	@MockBean
	private PaymentGateway paymentGateway;

	@BeforeEach
	void setUp()
	{
		orderItemRepository.deleteAll().block();
		orderRepository.deleteAll().block();
		cartRepository.deleteAll().block();
		itemRepository.deleteAll().block();
	}

	@Test
	void buy_success_createsOrderAndRedirectsToOrderPage()
	{
		Item item = saveItem("ok-item", 200L);

		cartService.updateCart(item.getId(), CartAction.PLUS).block();
		when(paymentGateway.charge(anyLong(), isNull())).thenReturn(Mono.empty());

		webTestClient.post()
				.uri("/buy")
				.exchange()
				.expectStatus().is3xxRedirection()
				.expectHeader().valueMatches(HttpHeaders.LOCATION, "/orders/\\d+\\?newOrder=true");

		assertThat(orderRepository.findAll().collectList().block()).hasSize(1);
		assertThat(cartRepository.findAll().collectList().block()).isEmpty();
	}

	@Test
	void buy_insufficientFunds_keepsCartAndRedirectsToCart()
	{
		Item item = saveItem("low-funds-item", 500L);

		cartService.updateCart(item.getId(), CartAction.PLUS).block();
		when(paymentGateway.charge(anyLong(), isNull())).thenReturn(Mono.error(new IllegalArgumentException("Insufficient funds")));

		webTestClient.post()
				.uri("/buy")
				.exchange()
				.expectStatus().is3xxRedirection()
				.expectHeader().valueEquals(HttpHeaders.LOCATION, "/cart/items?paymentError=insufficient");

		assertThat(orderRepository.findAll().collectList().block()).isEmpty();
		assertThat(cartRepository.findAll().collectList().block()).hasSize(1);
	}

	@Test
	void buy_paymentUnavailable_keepsCartAndRedirectsToCart() {
		Item item = saveItem("down-service-item", 300L);

		cartService.updateCart(item.getId(), CartAction.PLUS).block();
		when(paymentGateway.charge(anyLong(), isNull())).thenReturn(Mono.error(new IllegalStateException("down")));

		webTestClient.post()
				.uri("/buy")
				.exchange()
				.expectStatus().is3xxRedirection()
				.expectHeader().valueEquals(HttpHeaders.LOCATION, "/cart/items?paymentError=unavailable");

		assertThat(orderRepository.findAll().collectList().block()).isEmpty();
		assertThat(cartRepository.findAll().collectList().block()).hasSize(1);
	}

	private Item saveItem(String title, long price) {
		Item item = new Item();
		item.setTitle(title);
		item.setPrice(price);
		return itemRepository.save(item).block();
	}
}
