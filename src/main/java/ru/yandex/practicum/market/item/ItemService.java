package ru.yandex.practicum.market.item;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ItemService {

	Flux<Item> findAll(String search, String sort);

	Mono<Item> findById(Long id);
}
