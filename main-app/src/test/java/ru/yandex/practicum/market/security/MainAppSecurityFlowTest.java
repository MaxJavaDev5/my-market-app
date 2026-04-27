package ru.yandex.practicum.market.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class MainAppSecurityFlowTest
{

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void shouldServeCatalogPage_forAnonymous()
	{
		webTestClient.get().uri("/items").exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class)
				.value(html -> assertThat(html).contains("Витрина магазина"));
	}

	@Test
	void shouldRedirectToLogin_whenAnonymousAccessesCart()
	{
		webTestClient.get().uri("/cart/items").exchange()
				.expectStatus().is3xxRedirection()
				.expectHeader().value("Location", loc -> assertThat(loc).contains("/login"));
	}

	@Test
	void shouldRedirectToLogin_whenAnonymousAccessesOrders()
	{
		webTestClient
				.get().uri("/orders")
				.exchange()
				.expectStatus().is3xxRedirection()
				.expectHeader().value("Location", location ->
						assertThat(location).contains("/login"));
	}

	@Test
	void shouldShowDefaultLoginPage()
	{
		webTestClient.get().uri("/login").exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
				.expectBody(String.class)
				.value(body -> assertThat(body.toLowerCase()).contains("login"));
	}

	@Test
	void shouldAllowCartAccess_whenAuthenticated()
	{
		webTestClient.mutateWith(mockUser("user").roles("USER"))
				.get().uri("/cart/items")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
	}
}
