package ru.yandex.practicum.market.item;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {

	Flux<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
