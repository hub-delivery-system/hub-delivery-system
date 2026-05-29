package com.hubdelivery.global.config;

import com.hubdelivery.hubtohub.config.HubTransferCacheData;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.CacheKeyPrefix;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 전사 Redis 캐시 설정
 * - 일반 캐싱 설정 (@Cacheable, @CacheEvict 등)
 * - HubToHub 경로 캐싱용 RedisTemplate
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 기본 Redis 캐시 매니저
     * - @Cacheable 사용 시 기본 매니저
     * - TTL: 10분
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration configuration = RedisCacheConfiguration
                .defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(Duration.ofMinutes(10))
                .computePrefixWith(CacheKeyPrefix.simple())
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.java())
                );

        return RedisCacheManager.builder(
                redisConnectionFactory
        ).cacheDefaults(configuration).build();
    }

    /**
     * HubToHub 경로 캐싱용 RedisTemplate
     * - HubToHubCacheData 전용 직렬화
     * - 수동으로 cacheService에서 사용
     */
    @Bean
    public RedisTemplate<String, HubTransferCacheData> hubToHubRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, HubTransferCacheData> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key 직렬화: String
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 직렬화: Jackson2Json
        Jackson2JsonRedisSerializer<HubTransferCacheData> jackson2JsonRedisSerializer =
                new Jackson2JsonRedisSerializer<>(HubTransferCacheData.class);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
