package ru.yandex.practicum.market.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.cart.CartController;
import ru.yandex.practicum.market.cart.CartService;
import ru.yandex.practicum.market.config.SecurityConfig;
import ru.yandex.practicum.market.payment.PaymentGateway;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

@WebFluxTest(controllers = CartController.class)
@AutoConfigureWebTestClient
@Import({SecurityConfig.class, SecurityAccessTest.TestSecurityUsersConfig.class})
class SecurityAccessTest
{

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private CartService cartService;

	@MockBean
	private PaymentGateway paymentGateway;

	@Test
	void cartPage_redirectsToLogin_forAnonymousUser()
	{
		webTestClient.get().uri("/cart/items").exchange()
				.expectStatus().is3xxRedirection()
				.expectHeader().valueMatches("Location", ".*/login");
	}

	@Test
	void cartPage_returnsOk_forAuthenticatedUser()
	{
		when(cartService.getCartItems()).thenReturn(Flux.empty());
		when(cartService.getTotalSum()).thenReturn(Mono.just(0L));
		when(paymentGateway.getBalance()).thenReturn(Mono.just(100_000L));

		webTestClient.mutateWith(mockUser()).get().uri("/cart/items")
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);
	}

	@TestConfiguration
	static class TestSecurityUsersConfig {
		@Bean
		ReactiveUserDetailsService reactiveUserDetailsService() {
			return new MapReactiveUserDetailsService(
					User.withUsername("test-user")
							.password("{noop}test-password")
							.roles("USER")
							.build());
		}
	}
}
