package ru.yandex.practicum.market.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class ItemCacheServiceImpl implements ItemCacheService {

	private final TypeReference<List<Item>> ITEM_LIST_TYPE = new TypeReference<>() {};
	private final Duration CACHE_TTL = Duration.ofMinutes(10);

	private final RedisClient redisClient;
	private final ObjectMapper objectMapper;

	public ItemCacheServiceImpl(
			ObjectMapper objectMapper,
			@org.springframework.beans.factory.annotation.Value("${spring.data.redis.host:localhost}") String redisHost,
			@org.springframework.beans.factory.annotation.Value("${spring.data.redis.port:6379}") int redisPort)
	{
		this.objectMapper = objectMapper;
		this.redisClient = RedisClient.create(
				RedisURI.Builder.redis(redisHost, redisPort).build()
		);
	}

	@Override
	public Mono<List<Item>> getItemList(String search, String sort)
	{
		String searchKey = (search == null || search.isBlank())
				? "_"
				: search.trim().toLowerCase();

		String sortKey = (sort == null || sort.isBlank())
				? "NO"
				: sort.toUpperCase();

		String key = "items:list:" + searchKey + ":" + sortKey;
		
		return Mono.using(
				redisClient::connect,
				connection -> connection.reactive().get(key),
				StatefulRedisConnection::close
		).flatMap(this::deserializeList).onErrorResume(error -> Mono.empty());
	}

	@Override
	public Mono<Void> putItemList(String search, String sort, List<Item> items)
	{
		String searchKey = (search == null || search.isBlank())
				? "_"
				: search.trim().toLowerCase();

		String sortKey = (sort == null || sort.isBlank())
				? "NO"
				: sort.toUpperCase();

		String key = "items:list:" + searchKey + ":" + sortKey;

		return serialize(items)
				.flatMap(json -> Mono.using(
						redisClient::connect,
						connection -> connection.reactive().setex(key, CACHE_TTL.toSeconds(), json),
						StatefulRedisConnection::close
				)).then().onErrorResume(error -> Mono.empty());
	}

	@Override
	public Mono<Item> getItem(Long id)
	{
		String key = "items:item:" + id;

		return Mono.using(
				redisClient::connect,
				connection -> connection.reactive().get(key),
				StatefulRedisConnection::close
		).flatMap(this::deserializeItem).onErrorResume(error -> Mono.empty());
	}

	@Override
	public Mono<Void> putItem(Item item) {
		String key = "items:item:" + item.getId();
		return serialize(item)
				.flatMap(json -> Mono.using(
						redisClient::connect,
						connection -> connection.reactive().setex(key, CACHE_TTL.toSeconds(), json),
						StatefulRedisConnection::close
				)).then().onErrorResume(error -> Mono.empty());
	}

	private Mono<String> serialize(Object value) {
		try
		{
			return Mono.just(objectMapper.writeValueAsString(value));
		}
		catch (JsonProcessingException e)
		{
			return Mono.error(e);
		}
	}

	private Mono<List<Item>> deserializeList(String json) {
		try
		{
			return Mono.just(objectMapper.readValue(json, ITEM_LIST_TYPE));
		}
		catch (JsonProcessingException e)
		{
			return Mono.error(e);
		}
	}

	private Mono<Item> deserializeItem(String json) {
		try
		{
			return Mono.just(objectMapper.readValue(json, Item.class));
		}
		catch (JsonProcessingException e)
		{
			return Mono.error(e);
		}
	}

}
