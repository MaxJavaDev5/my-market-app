package ru.yandex.practicum.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = PaymentServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PaymentApiTest
{

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getBalance_returnsCurrentBalance() {
		webTestClient.get()
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

		webTestClient.post()
				.uri("/payments/charge")
				.bodyValue("{\"amount\":1000,\"orderId\":1}")
				.header("Content-Type", "application/json")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.success").isEqualTo(true);

		long after = getBalance();
		org.assertj.core.api.Assertions.assertThat(after).isEqualTo(before - 1000);
	}

	@Test
	void charge_returnsFailure_whenInsufficientFunds()
	{
		webTestClient.post()
				.uri("/payments/charge")
				.bodyValue("{\"amount\":999999999}")
				.header("Content-Type", "application/json")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.success").isEqualTo(false)
				.jsonPath("$.message").isEqualTo("Insufficient funds");
	}

	private long getBalance() {
		final long[] result = new long[1];

		webTestClient.get()
				.uri("/payments/balance")
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.balance")
				.value(value -> result[0] = ((Number) value).longValue());

		return result[0];
	}
}
