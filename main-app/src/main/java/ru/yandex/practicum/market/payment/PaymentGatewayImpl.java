package ru.yandex.practicum.market.payment;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.payment.client.api.DefaultApi;
import ru.yandex.practicum.market.payment.client.model.ChargeRequest;

@Service
public class PaymentGatewayImpl implements PaymentGateway {

	private final DefaultApi paymentsApi;
	private final PaymentProperties paymentProperties;

	public PaymentGatewayImpl(DefaultApi paymentsApi, PaymentProperties paymentProperties) {
		this.paymentsApi = paymentsApi;
		this.paymentProperties = paymentProperties;
	}

	@Override
	public Mono<Long> getBalance()
	{
		if (!paymentProperties.isEnabled())
		{
			return Mono.just(Long.MAX_VALUE);
		}
		return paymentsApi.getBalance()
				.switchIfEmpty(Mono.error(new IllegalStateException("Payment service returned empty response")))
				.map(resp -> resp.getBalance() != null ? resp.getBalance() : 0L)
				.onErrorMap(e -> new IllegalStateException("Payment service is unavailable", e));
	}

	@Override
	public Mono<Void> charge(long amount, Long orderId)
	{
		if (!paymentProperties.isEnabled())
		{
			return Mono.empty();
		}
		if (amount <= 0)
		{
			return Mono.empty();
		}

		ChargeRequest req = new ChargeRequest();
		req.setAmount(amount);
		req.setOrderId(orderId);

		return paymentsApi.charge(req)
				.switchIfEmpty(Mono.error(new IllegalStateException("Payment service returned empty response")))
				.flatMap(chargeResponse ->
				{
					if (Boolean.TRUE.equals(chargeResponse.getSuccess()))
					{
						return Mono.<Void>empty();
					}
					String msg = chargeResponse.getMessage() != null ? chargeResponse.getMessage() : "Payment declined";
					return Mono.error(new IllegalArgumentException(msg));
				})
				.onErrorMap(err -> {
					if (err instanceof IllegalArgumentException || err instanceof IllegalStateException) {
						return err;
					}
					return new IllegalStateException("Payment service is unavailable", err);
				});
	}
}
