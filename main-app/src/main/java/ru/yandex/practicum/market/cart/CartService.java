package ru.yandex.practicum.market.cart;

import reactor.core.publisher.Flux;
import ru.yandex.practicum.market.item.Item;
import reactor.core.publisher.Mono;

public interface CartService {

	Mono<Void> updateCart(Long itemId, CartAction action);

	Flux<Item> getCartItems();

	Mono<Long> getTotalSum();
}
