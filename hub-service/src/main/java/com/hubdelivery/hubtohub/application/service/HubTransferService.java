package com.hubdelivery.hubtohub.application.service;

import com.hubdelivery.common.response.PageResponse;
import com.hubdelivery.common.util.PageableUtils;
import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.exception.HubNotFoundException;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hubtohub.application.dto.ResGetHubTransferDto;
import com.hubdelivery.hubtohub.config.HubTransferCacheData;
import com.hubdelivery.hubtohub.domain.entity.HubTransferEntity;
import com.hubdelivery.hubtohub.domain.entity.HubTransferWaypointEntity;
import com.hubdelivery.hubtohub.domain.exception.HubTransferDuplicateLocationException;
import com.hubdelivery.hubtohub.domain.exception.HubTransferNotFoundException;
import com.hubdelivery.hubtohub.domain.repository.CentralHubRepository;
import com.hubdelivery.hubtohub.domain.repository.HubToHubWaypointRepository;
import com.hubdelivery.hubtohub.domain.repository.HubTransferRepository;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.response.DirectionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
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

    private final HubTransferRepository hubTransferRepository;
    private final HubToHubWaypointRepository waypointRepository;
    private final HubTransferCacheService cacheService;
    private final HubRouteService hubRouteService;
    private final HubRepository hubRepository;
    private final CentralHubRepository centralHubRepository;
    private final HubTransferCreateService hubTransferCreateService;



    @Transactional
    public ResGetHubTransferDto createHubTransfer(UUID userId,UUID fromHubId, UUID toHubId) {
        log.info("새 경로 생성 요청 - from: {} → to: {} , userId: {}", fromHubId, toHubId,userId.toString());

        // 1. Hub 존재 확인
        HubPair hubs = validateAndGetHubs(fromHubId, toHubId);
        HubEntity fromHub = hubs.fromHub();
        HubEntity toHub = hubs.toHub();

        // 2. 이미 존재하는 경로인지 확인
        boolean routeExists = hubTransferRepository.existsByStartHubIdAndEndHubIdAndDeletedAtIsNull(fromHubId, toHubId);
        log.info(String.valueOf(routeExists));
        if (routeExists) {
            log.warn("이미 존재하는 경로 - from: {} → to: {}", fromHub.getHubName(), toHub.getHubName());
            throw new HubTransferDuplicateLocationException(
                    "이미 존재하는 경로입니다: " + fromHub.getHubName() + " → " + toHub.getHubName()
            );
        }

        // 3. 새로운 경로 생성 및 저장
        ResGetHubTransferDto responseDto = hubTransferCreateService.createAndSaveNewRoute(fromHub, toHub);

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
    public PageResponse<ResGetHubTransferDto> getHubTransferList(
            UUID fromHubId,
            UUID toHubId,
            int page,
            int size,
            String sort,
            UUID userId) {

        log.info("경로 목록 조회 - fromHubId: {}, toHubId: {}, page: {}, size: {}, userId: {}",
                fromHubId, toHubId, page, size, userId.toString());

        // 1. Pageable 생성
        Pageable basePageable = PageableUtils.createPageable(page, size);
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(
                basePageable.getPageNumber(),
                basePageable.getPageSize(),
                sortObj
        );

        // 2. 1번의 쿼리: ID만 페이지네이션으로 조회
        Page<UUID> idPage = hubTransferRepository.findIdsByFilters(
                fromHubId, toHubId, pageable
        );

        // 3. 2번의 쿼리: 모든 관계를 함께 Fetch Join (waypoints + centralHub + hub)
        List<HubTransferEntity> entities = hubTransferRepository.findByIdsFetchWaypointsWithAll(
                idPage.getContent()
        );

        //4.  DTO로 변환 (쿼리 없음! 이미 모든 데이터 로드됨)
        List<ResGetHubTransferDto> dtoList = entities.stream()
                .map(hubTransferCreateService::buildResponseDtoFromFetchedEntity)
                .toList();

        //5.  PageResponse로 변환
        Page<ResGetHubTransferDto> dtoPage = new PageImpl<>(
                dtoList,
                pageable,
                idPage.getTotalElements()
        );

        return PageResponse.from(dtoPage);
    }

    /**
     * 정렬 문자열 파싱
     * 예: "createdAt,desc" → Sort.by("createdAt").descending()
     */
    private Sort parseSort(String sortParam) {
        String[] parts = sortParam.split(",");
        String property = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim().toLowerCase() : "asc";

        if ("desc".equals(direction)) {
            return Sort.by(property).descending();
        } else {
            return Sort.by(property).ascending();
        }
    }

    public ResGetHubTransferDto getHubRouteByTransferId(UUID transferId,UUID userId) {
        log.info("경로 조회 시작 - transferId: {}, userId : {}", transferId,userId.toString());

        // 2. DB에서 조회
        HubTransferEntity existingRoute = hubTransferRepository.findByIdFetchWaypoints(transferId)
                .orElseThrow(() -> {
                    log.warn("경로를 찾을 수 없음 - transferId: {}", transferId);
                    return new HubTransferNotFoundException();
                });

        validateAndGetHubs(existingRoute.getStartHubId(), existingRoute.getEndHubId());

        // 1. 캐시에서 transferId로 조회
        HubTransferCacheData cachedData = cacheService.getRouteByTransferId(transferId);
        if (cachedData != null) {
            log.info("캐시에서 경로 조회 성공 - transferId: {}", transferId);
            return cachedData.toResponseDto();
        }

        log.info("DB에서 경로 조회 성공 - routeId: {}", transferId);

        // 3. ResponseDto 생성
        ResGetHubTransferDto responseDto = hubTransferCreateService.buildResponseDtoFromFetchedEntity(existingRoute);

        // 4. transferId로 캐시에 저장
        hubTransferCreateService.cacheRouteByTransferId(transferId, responseDto);

        return responseDto;
    }

    /**
     * 경로 업데이트
     * - 기존 경로를 카카오 API로 재계산
     * - Entity와 경유지 정보 수정
     * - 캐시 갱신
     *
     * @param transferId 경로 ID
     * @return 업데이트된 경로 정보
     */
    @Transactional
    public ResGetHubTransferDto updateHubRouteByTransferId(UUID transferId,UUID userId) {
        log.info("경로 업데이트 시작 - transferId: {}, userId : {}", transferId, userId.toString());

        // 1. DB에서 기존 경로 조회
        HubTransferEntity existingRoute = hubTransferRepository.findById(transferId)
                .orElseThrow(() -> {
                    log.warn("경로를 찾을 수 없음 - transferId: {}", transferId);
                    return new HubTransferNotFoundException();
                });

        // 2. Hub 존재 확인
        HubPair hubs = validateAndGetHubs(existingRoute.getStartHubId(), existingRoute.getEndHubId());
        HubEntity fromHub = hubs.fromHub();
        HubEntity toHub = hubs.toHub();

        // 3. 카카오 API로 새 경로 계산
        DirectionsResponse directionsResponse = hubRouteService.calculateRoute(fromHub, toHub);
        DirectionsResponse.Summary summary = directionsResponse.getRoutes().get(0).getSummary();

        BigDecimal distanceKm = BigDecimal.valueOf(summary.getDistance())
                .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
        long durationSec = summary.getDuration();

        log.debug("새 경로 정보 파싱 완료 - 거리: {}km, 시간: {}분", distanceKm, (durationSec / 60));

        // 4. Entity 업데이트
        existingRoute.updateRoute(distanceKm, durationSec);
        HubTransferEntity updatedEntity = hubTransferRepository.save(existingRoute);
        log.debug("경로 정보 업데이트 완료 - routeId: {}", transferId);

        // 5. 기존 경유지 삭제
        waypointRepository.deleteByHubToHubId(transferId);
        log.debug("기존 경유지 삭제 완료");

        // 6. 새로운 경유지 저장
        hubTransferCreateService.createAndSaveWaypoints(existingRoute, fromHub, toHub);
        log.debug("새 경유지 저장 완료");

        // 7. ResponseDto 생성
        ResGetHubTransferDto responseDto = hubTransferCreateService.buildResponseDtoFromDb(updatedEntity);

        // 8. 캐시 갱신 (두 가지 키로 모두 저장)
        hubTransferCreateService.cacheTransfer(transferId,responseDto);

        log.info("경로 업데이트 완료 - routeId: {}", transferId);
        return responseDto;
    }

    /**
     * 경로 삭제 (Soft Delete)
     * - 경로 정보를 soft delete 처리
     * - 경유지 정보도 soft delete 처리
     * - 캐시 제거
     *
     * @param transferId 경로 ID
     */
    @Transactional
    public void deleteHubRouteByTransferId(UUID userId,UUID transferId) {
        log.info("경로 삭제 시작 - transferId: {}, userId : {}", transferId, userId.toString());

        // 1. DB에서 기존 경로 조회
        HubTransferEntity existingRoute = hubTransferRepository.findById(transferId)
                .orElseThrow(() -> {
                    log.warn("경로를 찾을 수 없음 - transferId: {}", transferId);
                    return new HubTransferNotFoundException();
                });

        UUID startHubId = existingRoute.getStartHubId();
        UUID endHubId = existingRoute.getEndHubId();

        // 2. 경로 soft delete
        existingRoute.softDelete(userId.toString());
        hubTransferRepository.save(existingRoute);
        log.debug("경로 soft delete 완료 - routeId: {}", transferId);

        // 3. 경유지 soft delete
        List<HubTransferWaypointEntity> waypointEntities = waypointRepository
                .findByHubToHubIdOrderBySequence(transferId);

        waypointEntities.forEach(waypoint -> waypoint.softDelete(userId.toString()));
        waypointRepository.saveAll(waypointEntities);
        log.debug("경유지 soft delete 완료 - 경유지 수: {}", waypointEntities.size());

        // 4. 캐시 제거
        try {
            cacheService.evictRoute(startHubId, endHubId);
            cacheService.evictRouteByTransferId(transferId);
            log.debug("경로 캐시 제거 완료");
        } catch (Exception e) {
            log.warn("경로 캐시 제거 실패 (계속 진행)", e);
        }

        log.info("경로 삭제 완료 - transferId: {}", transferId);
    }

    public record HubPair(HubEntity fromHub, HubEntity toHub) {}

    /**
     * 두 허브의 존재 여부 검증
     *
     * @param fromHubId 출발 허브 ID
     * @param toHubId 도착 허브 ID
     * @return HubPair (fromHub, toHub)
     * @throws HubNotFoundException 허브를 찾을 수 없을 때
     */
    private HubPair validateAndGetHubs(UUID fromHubId, UUID toHubId) {
        HubEntity fromHub = hubRepository.findByIdActive(fromHubId)
                .orElseThrow(HubNotFoundException::new);
        HubEntity toHub = hubRepository.findByIdActive(toHubId)
                .orElseThrow(HubNotFoundException::new);

        return new HubPair(fromHub, toHub);
    }

}

