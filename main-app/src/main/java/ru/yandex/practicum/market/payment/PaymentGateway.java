package ru.yandex.practicum.market.payment;

import reactor.core.publisher.Mono;

public interface PaymentGateway
{
	Mono<Long> getBalance();
	Mono<Void> charge(long amount, Long orderId);
}

