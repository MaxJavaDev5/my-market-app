package ru.yandex.practicum.market.item;

import ru.yandex.practicum.market.cart.CartAction;
import ru.yandex.practicum.market.cart.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ItemControllerTest {

	@Autowired
	WebTestClient webTestClient;

	@MockBean
	ItemService itemService;

	@MockBean
	CartService cartService;

	@Test
	void getItems_returnsItemsView() {
		when(itemService.findAll(null, "NO")).thenReturn(Mono.just(List.of()));

		webTestClient.get().uri("/items")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
	}

	@Test
	void getRoot_returnsItemsView() {
		when(itemService.findAll(null, "NO")).thenReturn(Mono.just(List.of()));

		webTestClient.get().uri("/")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);

		verify(itemService).findAll(null, "NO");
	}

	@Test
	void getItems_passesSearchAndSortToService() {
		when(itemService.findAll("Product_Query", "PRICE")).thenReturn(Mono.just(List.of()));

		webTestClient.get().uri(uriBuilder -> uriBuilder.path("/items")
						.queryParam("search", "Product_Query")
						.queryParam("sort", "PRICE")
						.build())
				.exchange()
				.expectStatus().isOk();

		verify(itemService).findAll("Product_Query", "PRICE");
	}

	@Test
	void getItem_returnsItemView() {
		Item item = new Item();
		item.setId(1L);
		item.setTitle("Product");
		item.setPrice(500L);
		when(itemService.findById(1L)).thenReturn(Mono.just(item));

		webTestClient.get().uri("/items/1")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
	}

	@Test
	void postItems_redirectWhenSearchMissing_usesEmptySearchInUrl() {
		when(cartService.updateCart(anyLong(), any())).thenReturn(Mono.empty());

		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/items")
						.queryParam("id", "1")
						.queryParam("action", "MINUS")
						.queryParam("sort", "NO")
						.queryParam("pageNumber", "1")
						.queryParam("pageSize", "5")
						.build())
				.exchange()
				.expectStatus().is3xxRedirection()
				.expectHeader().value("Location", uri ->
						assertThat(uri).endsWith("/items?search=&sort=NO&pageNumber=1&pageSize=5"));

		verify(cartService).updateCart(1L, CartAction.MINUS);
	}

	@Test
	void postItemById_updatesCartAndReturnsItemView() {
		Item item = new Item();
		item.setId(7L);
		item.setTitle("Product_Seven");
		item.setPrice(100L);
		when(cartService.updateCart(anyLong(), any())).thenReturn(Mono.empty());
		when(itemService.findById(7L)).thenReturn(Mono.just(item));

		webTestClient.post()
				.uri(uriBuilder -> uriBuilder.path("/items/7")
						.queryParam("action", "PLUS")
						.build())
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);

		verify(cartService).updateCart(7L, CartAction.PLUS);
		verify(itemService).findById(7L);
	}
}
