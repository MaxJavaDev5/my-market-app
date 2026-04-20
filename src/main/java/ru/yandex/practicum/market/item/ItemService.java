package ru.yandex.practicum.market.item;

import reactor.core.publisher.Mono;

import java.util.List;

public interface ItemService {

	Mono<List<Item>> findAll(String search, String sort);

	Mono<Item> findById(Long id);
}
