package ru.yandex.practicum.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.payment.api.DefaultApi;
import ru.yandex.practicum.payment.model.BalanceResponse;
import ru.yandex.practicum.payment.model.ChargeRequest;
import ru.yandex.practicum.payment.model.ChargeResponse;
import ru.yandex.practicum.payment.service.PaymentBalanceService;

@RestController
public class PaymentsController implements DefaultApi {

	private final PaymentBalanceService paymentBalanceService;

	public PaymentsController(PaymentBalanceService paymentBalanceService)
	{
		this.paymentBalanceService = paymentBalanceService;
	}

	@Override
	public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange)
	{
		return Mono.just(ResponseEntity.ok(paymentBalanceService.getBalance()));
	}

	@Override
	public Mono<ResponseEntity<ChargeResponse>> charge(Mono<ChargeRequest> chargeRequest, ServerWebExchange exchange)
	{
		return chargeRequest
				.map(request -> paymentBalanceService.charge(request.getAmount()))
				.map(ResponseEntity::ok);
	}
}
