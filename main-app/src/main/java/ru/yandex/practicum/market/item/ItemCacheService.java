package ru.yandex.practicum.market.item;

import reactor.core.publisher.Mono;

import java.util.List;

public interface ItemCacheService {

	Mono<List<Item>> getItemList(String search, String sort);

	Mono<Void> putItemList(String search, String sort, List<Item> items);

	Mono<Item> getItem(Long id);

	Mono<Void> putItem(Item item);
}
