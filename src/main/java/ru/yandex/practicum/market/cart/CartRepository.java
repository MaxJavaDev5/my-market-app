package ru.yandex.practicum.market.cart;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CartRepository extends ReactiveCrudRepository<CartItem, Long> {

	Mono<CartItem> findByItemId(Long itemId);
}
