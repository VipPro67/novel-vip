package com.novel.vippro.Config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

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

	private ObjectMapper createRedisObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.activateDefaultTyping(
				mapper.getPolymorphicTypeValidator(),
				ObjectMapper.DefaultTyping.EVERYTHING,
				JsonTypeInfo.As.PROPERTY);
		return mapper;
	}

	private RedisSerializer<Object> redisValueSerializer() {
		return new LenientGenericJackson2JsonRedisSerializer(createRedisObjectMapper());
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
		RedisSerializer<Object> serializer = redisValueSerializer();

		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(factory);

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(serializer);
		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(serializer);

		template.afterPropertiesSet();
		return template;
	}

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory factory) {
		RedisSerializer<Object> serializer = redisValueSerializer();

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
				.serializeKeysWith(
						RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(serializer))
				.entryTtl(Duration.ofMinutes(10));

		Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
		cacheConfigs.put("novels", config.entryTtl(Duration.ofHours(6)));
		cacheConfigs.put("chapters", config.entryTtl(Duration.ofHours(6)));
		cacheConfigs.put("users", config.entryTtl(Duration.ofDays(1)));

		return RedisCacheManager.builder(factory)
				.cacheDefaults(config)
				.withInitialCacheConfigurations(cacheConfigs)
				.build();
	}

	private static final class LenientGenericJackson2JsonRedisSerializer extends GenericJackson2JsonRedisSerializer {

		LenientGenericJackson2JsonRedisSerializer(ObjectMapper mapper) {
			super(mapper);
		}

		@Override
		public Object deserialize(byte[] bytes) {
			try {
				return super.deserialize(bytes);
			} catch (SerializationException ex) {
				if (containsInvalidTypeId(ex)) {
					return null;
				}
				throw ex;
			}
		}

		private boolean containsInvalidTypeId(Throwable throwable) {
			while (throwable != null) {
				if (throwable instanceof InvalidTypeIdException) {
					return true;
				}
				throwable = throwable.getCause();
			}
			return false;
		}
	}

}
