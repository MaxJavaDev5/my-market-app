package ru.yandex.practicum.market.auth;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, Long>
{
	Mono<UserEntity> findByUsername(String username);
}
