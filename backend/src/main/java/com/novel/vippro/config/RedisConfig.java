package com.novel.vippro.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

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