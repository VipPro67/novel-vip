package com.novel.vippro.Config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {
    @Value("${REDIS_HOST}")
    private String redisHost;

    @Value("${REDIS_PORT}")
    private int redisPort;

    @Value("${REDIS_USERNAME:}")
    private String redisUsername;

    @Value("${REDIS_PASSWORD:}")
    private String redisPassword;

    @Value("${REDIS_SSL:false}")
    private boolean redisSsl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        if (redisUsername != null && !redisUsername.isBlank()) {
            config.setUsername(redisUsername);
        }
        if (redisPassword != null && !redisPassword.isBlank()) {
            config.setPassword(RedisPassword.of(redisPassword));
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().useSsl()
            .build();

        return new LettuceConnectionFactory(config, clientConfig);
    }

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		// Configure Jackson serializer with type information and JSR-310 support
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.activateDefaultTyping(
				objectMapper.getPolymorphicTypeValidator(),
				ObjectMapper.DefaultTyping.NON_FINAL,
				JsonTypeInfo.As.PROPERTY);
		Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,
				Object.class);

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(serializer);
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(serializer);
		template.afterPropertiesSet();

		return template;
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory factory) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.activateDefaultTyping(
				objectMapper.getPolymorphicTypeValidator(),
				ObjectMapper.DefaultTyping.NON_FINAL,
				JsonTypeInfo.As.PROPERTY);
		Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper,
				Object.class);

		RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(RedisSerializationContext.SerializationPair
						.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(serializer))
				.entryTtl(Duration.ofMinutes(10));

		return RedisCacheManager.builder(factory)
				.cacheDefaults(cacheConfig)
				.build();
	}
}