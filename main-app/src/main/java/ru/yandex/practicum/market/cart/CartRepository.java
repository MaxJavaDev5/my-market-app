package ru.yandex.practicum.market.cart;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CartRepository extends ReactiveCrudRepository<CartItem, Long> {

	Mono<CartItem> findByItemId(Long itemId);

	Mono<CartItem> findByUserIdAndItemId(Long userId, Long itemId);

	Flux<CartItem> findAllByUserId(Long userId);

	Mono<Void> deleteAllByUserId(Long userId);
}
