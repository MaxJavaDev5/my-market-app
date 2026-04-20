package ru.yandex.practicum.market.payment;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.payment.client.api.PaymentsApi;
import ru.yandex.practicum.market.payment.client.model.ChargeRequest;

@Service
public class PaymentGatewayImpl implements PaymentGateway 
{

	private final PaymentsApi paymentsApi;
	private final PaymentProperties paymentProperties;

	public PaymentGatewayImpl(PaymentsApi paymentsApi, PaymentProperties paymentProperties) 
	{
		this.paymentsApi = paymentsApi;
		this.paymentProperties = paymentProperties;
	}

	@Override
	public Mono<Void> charge(long amount, Long orderId) 
	{
		return paymentsApi.getBalance()
				.flatMap(balance -> 
				{
					Long currentBalance = balance.getBalance();

					if (currentBalance == null) 
					{
						return Mono.error(new IllegalStateException("Payment service returned empty balance"));
					}

					if (currentBalance < amount) 
					{
						return Mono.error(new IllegalArgumentException("Not enough money"));
					}

					ChargeRequest request = new ChargeRequest();
					request.setAmount(amount);
					request.setOrderId(orderId);
					return paymentsApi.charge(request);
				})
				.flatMap(chargeResponse -> 
				{
					if (Boolean.TRUE.equals(chargeResponse.getSuccess())) {
						return Mono.<Void>empty();
					}
					return Mono.error(new IllegalArgumentException(chargeResponse.getMessage()));
				})
				.switchIfEmpty(Mono.error(new IllegalStateException("Payment service returned empty balance")))
				.onErrorMap(error -> {
					if (error instanceof IllegalArgumentException || error instanceof IllegalStateException) {
						return error;
					}
					return new IllegalStateException("Payment service is unavailable", error);
				});
	}
}
