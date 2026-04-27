package ru.yandex.practicum.market.order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrderService {

	Mono<Order> createOrder();

	Mono<Order> findById(Long id);

	Flux<Order> findAll();
}
