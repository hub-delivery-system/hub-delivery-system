package com.hubdelivery.hub.application.service;

import com.hubdelivery.hub.application.dto.ResGetHubDto;
import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.exception.HubDuplicateLocationException;
import com.hubdelivery.hub.domain.exception.HubNotFoundException;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hub.domain.type.UserRole;
import com.hubdelivery.hub.presentation.dto.ReqHubDto;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional( readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class HubServiceV1 implements HubService {

    private final HubRepository hubRepository;
    private final MeterRegistry meterRegistry;


    @Override
    @Transactional
    @CachePut(cacheNames="hubCache", key ="#result.hubId")
    @CacheEvict(cacheNames = "hubAllCache",allEntries = true)
    @Timed(value = "hub.create", description = "Hub 생성 시간")
    public ResGetHubDto createHub(ReqHubDto reqHubDto,UUID userId, UserRole role) {
        log.info("허브 생성 요청 - userId: {}, hubName: {}", userId, reqHubDto.getHub_name());

        try {
            validateDuplicateLocation(reqHubDto.getLatitude(), reqHubDto.getLongitude());

            HubEntity hubEntity = HubEntity.builder()
                    .hubName(reqHubDto.getHub_name())
                    .address(reqHubDto.getAddress())
                    .latitude(reqHubDto.getLatitude())
                    .longitude(reqHubDto.getLongitude())
                    .build();

            HubEntity saved = hubRepository.save(hubEntity);

            // ✅ 성공 카운터
            meterRegistry.counter("hub.create.success").increment();

            log.info("허브 생성 성공 - hubId: {}, hubName: {}", saved.getId(), saved.getHubName());
            return ResGetHubDto.from(saved);

        } catch (Exception e) {
            meterRegistry.counter("hub.create.failure", "reason", e.getClass().getSimpleName()).increment();
            log.error("허브 생성 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw e;
        }
    }


    @Override
    @Cacheable(
            cacheNames = "hubAllCache",
            key = "(#keyword ?: 'all') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize"
    )
    @Timed(value = "hub.getAll", description = "Hub 목록 조회 시간")
    public Page<ResGetHubDto> getHubs(String keyword, Pageable pageable) {
        log.info("허브 목록 조회 - page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());


        Page<ResGetHubDto> result = hubRepository.searchHubs(keyword,pageable)
                .map(ResGetHubDto::from);

        log.info("허브 목록 조회 완료 - 총 {}개, 페이지: {}/{}",
                result.getTotalElements(),
                result.getNumber() + 1,
                result.getTotalPages());

        return result;
    }

    @Override
    @Cacheable(cacheNames = "hubCache", key = "#hubId")
    @Timed(value = "hub.get", description = "Hub 조회 시간")
    public ResGetHubDto getHub(UUID hubId) {
        log.info("허브 조회 요청 - hubId: {}", hubId);
        HubEntity hubEntity = getHubEntityByHubId(hubId);

        log.debug("허브 조회 성공 - hubId: {}, hubName: {}", hubId, hubEntity.getHubName());
        return ResGetHubDto.from(hubEntity);
    }



    @Override
    @Transactional
    @Caching(
            put = {
                    @CachePut(cacheNames = "hubCache", key = "#hubId")
            },
            evict = {
                    @CacheEvict(cacheNames = "hubAllCache", allEntries = true)
            }
    )
    @Timed(value = "hub.update", description = "Hub 수정 시간")
    public ResGetHubDto updateHub(UUID hubId, ReqHubDto reqHubDto,UUID userId, UserRole role) {
        log.info("허브 수정 요청 - hubId: {}, userId: {}", hubId, userId);
        HubEntity hubEntity = getHubEntityByHubId(hubId);

        validateDuplicateLocationExcludingSelf(
                reqHubDto.getLatitude(),
                reqHubDto.getLongitude(),
                hubId
        );

        hubEntity.update(
                reqHubDto.getHub_name(),
                reqHubDto.getAddress(),
                reqHubDto.getLatitude(),
                reqHubDto.getLongitude());
        meterRegistry.counter("hub.update.success").increment();
        log.info("허브 수정 성공 - hubId: {}", hubId);
        return ResGetHubDto.from(hubEntity);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "hubAllCache", allEntries = true),
            @CacheEvict(cacheNames = "hubCache", key = "#hubId")
    })
    @Transactional
    @Timed(value = "hub.delete", description = "Hub 삭제 시간")
    public void deleteHub(UUID hubId,UUID userId, UserRole role) {
        log.warn("허브 삭제 요청 - hubId: {}, userId: {}, userRole: {}", hubId, userId,role);
        HubEntity hubEntity=getHubEntityByHubId(hubId);
        hubEntity.softDelete(userId.toString());
        meterRegistry.counter("hub.delete.success").increment();
        log.warn("허브 삭제 완료 (Soft Delete) - hubId: {}", hubId);
    }

    public HubEntity getHubEntityByHubId(UUID hubId) {
        return hubRepository.findById(hubId).orElseThrow(() -> {
            log.warn("허브를 찾을 수 없음 - hubId: {}", hubId);
            return new HubNotFoundException();
        });
    }

    public void validateDuplicateLocation(BigDecimal latitude, BigDecimal longitude) {
        if (hubRepository.existsByLatitudeAndLongitude(latitude, longitude)) {
            log.warn("중복된 위치로 허브 생성 시도 - lat: {}, lng: {}", latitude, longitude);
            throw new HubDuplicateLocationException();
        }
    }

    private void validateDuplicateLocationExcludingSelf(
            BigDecimal latitude,
            BigDecimal longitude,
            UUID excludeId
    ) {
        if (hubRepository.existsByLatitudeAndLongitudeExcludingId(latitude, longitude, excludeId)) {
            log.warn("다른 허브와 위치 중복 - lat: {}, lng: {}", latitude, longitude);
            throw new HubDuplicateLocationException();
        }
    }


}
