package ru.yandex.practicum.market.payment;

import reactor.core.publisher.Mono;

public interface PaymentGateway {

	Mono<Void> charge(long amount, Long orderId);
}
