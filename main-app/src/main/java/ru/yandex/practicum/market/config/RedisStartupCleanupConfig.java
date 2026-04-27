package ru.yandex.practicum.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;

@Configuration
public class RedisStartupCleanupConfig {

	@Bean
	public ApplicationRunner redisCacheCleanupRunner(
			@Value("${spring.data.redis.host:localhost}") String redisHost,
			@Value("${spring.data.redis.port:6379}") int redisPort)
	{
		return args ->
		{
			RedisClient client = RedisClient.create(RedisURI.Builder.redis(redisHost, redisPort).build());
			try (StatefulRedisConnection<String, String> conn = client.connect())
			{
				conn.sync().flushdb();
			} finally
			{
				client.shutdown();
			}
		};
	}
}
