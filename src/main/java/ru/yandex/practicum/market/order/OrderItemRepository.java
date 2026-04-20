package ru.yandex.practicum.market.order;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {

	Flux<OrderItem> findByOrderId(Long orderId);
}
