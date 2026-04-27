package ru.yandex.practicum.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(classes = PaymentServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PaymentApiTest
{

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getBalance_returnsUnauthorized_whenTokenMissing()
	{
		webTestClient.get().uri("/payments/balance").exchange().expectStatus().isUnauthorized();
	}

	@Test
	void getBalance_returnsCurrentBalance() {
		webTestClient.mutateWith(mockJwt()).get()
				.uri("/payments/balance")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.balance").isNumber();
	}

	@Test
	void charge_reducesBalance_whenEnoughFunds()
	{
		long before = getBalance();

		webTestClient.mutateWith(mockJwt())
				.mutateWith(csrf())
				.post()
				.uri("/payments/charge")
				.bodyValue("{\"amount\":1000,\"orderId\":1}")
				.header("Content-Type", "application/json")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.success").isEqualTo(true);

		long after = getBalance();
		assertThat(after).isEqualTo(before - 1000);
	}

	@Test
	void charge_returnsFailure_whenInsufficientFunds()
	{
		webTestClient.mutateWith(mockJwt())
				.mutateWith(csrf())
				.post()
				.uri("/payments/charge")
				.bodyValue("{\"amount\":999999999}")
				.header("Content-Type", "application/json")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.success").isEqualTo(false)
				.jsonPath("$.message").isEqualTo("Insufficient funds");
	}

	private long getBalance()
	{
		final long[] holder = new long[1];

		webTestClient.mutateWith(mockJwt()).get()
				.uri("/payments/balance")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.balance")
				.value(v -> holder[0] = ((Number) v).longValue());

		return holder[0];
	}
}
