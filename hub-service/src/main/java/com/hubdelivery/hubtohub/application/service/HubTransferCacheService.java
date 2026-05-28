package com.hubdelivery.hubtohub.application.service;

import com.hubdelivery.hubtohub.config.HubTransferCacheData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 HubToHub 경로 정보 캐싱
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HubTransferCacheService {

    private final RedisTemplate<String, HubTransferCacheData> redisTemplate;

    /**
     * 캐시 키 생성
     * 예: hubToHub:route:550e8400-e29b-41d4-a716-446655440000:660e8400-e29b-41d4-a716-446655440001
     */
    private String generateCacheKey(UUID startHubId, UUID endHubId) {
        return String.format("hubToHub:route:%s:%s", startHubId, endHubId);
    }

    /**
     * 캐시에 경로 정보 저장
     *
     * @param startHubId 출발 허브 ID
     * @param endHubId 도착 허브 ID
     * @param cacheData 캐시 데이터
     * @param ttlMinutes TTL (분)
     */
    public void cacheRoute(UUID startHubId, UUID endHubId, HubTransferCacheData cacheData, long ttlMinutes) {
        try {
            String key = generateCacheKey(startHubId, endHubId);
            redisTemplate.opsForValue().set(key, cacheData, ttlMinutes, TimeUnit.MINUTES);
            log.info("경로 정보 캐시 저장 - key: {}, TTL: {}분", key, ttlMinutes);
        } catch (Exception e) {
            log.error("캐시 저장 실패", e);
            // 캐시 실패해도 계속 진행 (캐시는 선택사항)
        }
    }

    /**
     * 캐시에서 경로 정보 조회
     *
     * @param startHubId 출발 허브 ID
     * @param endHubId 도착 허브 ID
     * @return 캐시된 경로 정보 (없으면 null)
     */
    public HubTransferCacheData getRoute(UUID startHubId, UUID endHubId) {
        try {
            String key = generateCacheKey(startHubId, endHubId);
            HubTransferCacheData cachedData = redisTemplate.opsForValue().get(key);
            if (cachedData != null) {
                log.info("경로 정보 캐시 히트 - key: {}", key);
            }
            return cachedData;
        } catch (Exception e) {
            log.error("캐시 조회 실패", e);
            return null;
        }
    }

    /**
     * 캐시 삭제
     *
     * @param startHubId 출발 허브 ID
     * @param endHubId 도착 허브 ID
     */
    public void invalidateCache(UUID startHubId, UUID endHubId) {
        try {
            String key = generateCacheKey(startHubId, endHubId);
            Boolean deleted = redisTemplate.delete(key);
            log.info("경로 정보 캐시 삭제 - key: {}, deleted: {}", key, deleted);
        } catch (Exception e) {
            log.error("캐시 삭제 실패", e);
        }
    }

    public HubTransferCacheData getRouteByTransferId(UUID transferId) {
        String key = "transfer:" + transferId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * transferId로 캐시에 저장
     */
    public void cacheRouteByTransferId(UUID transferId, HubTransferCacheData cacheData, long ttlMinutes) {
        String key = "transfer:" + transferId;
        redisTemplate.opsForValue().set(key, cacheData, Duration.ofMinutes(ttlMinutes));
    }

    /**
     * transferId로 캐시 삭제
     */
    public void evictRouteByTransferId(UUID transferId) {
        String key = "transfer:" + transferId;
        redisTemplate.delete(key);
    }
}
