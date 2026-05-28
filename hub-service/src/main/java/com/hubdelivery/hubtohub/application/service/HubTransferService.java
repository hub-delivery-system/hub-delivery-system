package com.hubdelivery.hubtohub.application.service;

import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.exception.HubNotFoundException;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hubtohub.application.dto.ResGetHubTransferDto;
import com.hubdelivery.hubtohub.config.HubTransferCacheData;
import com.hubdelivery.hubtohub.domain.entity.HubTransferEntity;
import com.hubdelivery.hubtohub.domain.exception.HubTransferDuplicateLocationException;
import com.hubdelivery.hubtohub.domain.exception.HubTransferNotFoundException;
import com.hubdelivery.hubtohub.domain.repository.CentralHubRepository;
import com.hubdelivery.hubtohub.domain.repository.HubToHubWaypointRepository;
import com.hubdelivery.hubtohub.domain.repository.HubTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 허브 간 경로 CRUD 및 캐싱 서비스
 * - 기존 경로 조회 (캐시 → DB)
 * - 새로운 경로 생성 및 저장
 * - DirectionsResponse 처리
 * - Redis 캐싱
 *
 * 책임: 경로 데이터 관리 및 캐싱 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubTransferService {

    private final HubTransferRepository hubToHubRepository;
    private final HubToHubWaypointRepository waypointRepository;
    private final HubTransferCacheService cacheService;
    private final HubRouteService hubRouteService;
    private final HubRepository hubRepository;
    private final CentralHubRepository centralHubRepository;
    private final HubTransferCreateService hubTransferCreateService;



    @Transactional
    public ResGetHubTransferDto createHubTransfer(UUID fromHubId, UUID toHubId) {
        log.info("새 경로 생성 요청 - from: {} → to: {}", fromHubId, toHubId);

        // 1. Hub 존재 확인
        HubEntity fromHub = hubRepository.findById(fromHubId)
                .orElseThrow(HubNotFoundException::new);
        HubEntity toHub = hubRepository.findById(toHubId)
                .orElseThrow(HubNotFoundException::new);

        // 2. 이미 존재하는 경로인지 확인
        boolean routeExists = hubToHubRepository.existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(fromHubId, toHubId);
        if (routeExists) {
            log.warn("이미 존재하는 경로 - from: {} → to: {}", fromHub.getHubName(), toHub.getHubName());
            throw new HubTransferDuplicateLocationException(
                    "이미 존재하는 경로입니다: " + fromHub.getHubName() + " → " + toHub.getHubName()
            );
        }

        // 3. 새로운 경로 생성 및 저장
        ResGetHubTransferDto responseDto = hubTransferCreateService.createAndSaveNewRoute(fromHub, toHub);
        log.info("새 경로 생성 완료 - routeId: {}", responseDto.getRouteId());

        return responseDto;
    }

    /**
     * 두 허브 간 경로 조회 또는 생성
     * 1. 캐시 확인
     * 2. DB 확인
     *
     * @param fromHubId 출발 허브
     * @param toHubId 도착 허브
     * @return 경로 정보 (경유지 포함)
     */
    public ResGetHubTransferDto getHubRouteByHubId(UUID fromHubId, UUID toHubId) {
        log.info("경로 조회 시작 - from: {} → to: {}", fromHubId, toHubId);

        // 1. Hub 존재 확인
        HubEntity fromHub = hubRepository.findById(fromHubId)
                .orElseThrow(HubNotFoundException::new);
        HubEntity toHub = hubRepository.findById(toHubId)
                .orElseThrow(HubNotFoundException::new);

        // 2. 캐시에서 조회
        HubTransferCacheData cachedData = cacheService.getRoute(fromHubId, toHubId);
        if (cachedData != null) {
            log.info("캐시에서 경로 조회 성공");
            return cachedData.toResponseDto();
        }

        // 3. DB에서 조회
        HubTransferEntity existingRoute = hubToHubRepository.findByStartHubIdAndEndHubId(
                fromHubId, toHubId
        ).orElse(null);  // ← 이제 null을 반환하거나 값을 반환


        if (existingRoute != null) {
            log.info("DB에서 경로 조회 성공");
            ResGetHubTransferDto responseDto = hubTransferCreateService.buildResponseDtoFromDb(existingRoute);
            // 캐시에 저장 (실패해도 계속 진행)
            hubTransferCreateService.cacheRoute(responseDto);
            return responseDto;
        }

        // 4. 경로를 찾을 수 없음
        log.warn("경로를 찾을 수 없음 - from: {} → to: {}", fromHub.getHubName(), toHub.getHubName());
        throw new HubTransferNotFoundException();
    }

    public ResGetHubTransferDto getHubRouteByTransferId(UUID transferId) {
        log.info("경로 조회 시작 - transferId: {}", transferId);

        // 1. 캐시에서 transferId로 조회
        HubTransferCacheData cachedData = cacheService.getRouteByTransferId(transferId);
        if (cachedData != null) {
            log.info("캐시에서 경로 조회 성공 - transferId: {}", transferId);
            return cachedData.toResponseDto();
        }

        // 2. DB에서 조회
        HubTransferEntity existingRoute = hubToHubRepository.findById(transferId)
                .orElseThrow(() -> {
                    log.warn("경로를 찾을 수 없음 - transferId: {}", transferId);
                    return new HubTransferNotFoundException();
                });

        log.info("DB에서 경로 조회 성공 - routeId: {}", transferId);

        // 3. ResponseDto 생성
        ResGetHubTransferDto responseDto = hubTransferCreateService.buildResponseDtoFromDb(existingRoute);

        // 4. transferId로 캐시에 저장
        hubTransferCreateService.cacheRouteByTransferId(transferId, responseDto);

        return responseDto;
    }




}

