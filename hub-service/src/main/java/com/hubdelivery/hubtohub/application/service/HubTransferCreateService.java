package com.hubdelivery.hubtohub.application.service;

import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hubtohub.application.dto.ResGetHubTransferDto;
import com.hubdelivery.hubtohub.config.HubTransferCacheData;
import com.hubdelivery.hubtohub.domain.entity.CentralHubEntity;
import com.hubdelivery.hubtohub.domain.entity.HubTransferEntity;
import com.hubdelivery.hubtohub.domain.entity.HubTransferWaypointEntity;
import com.hubdelivery.hubtohub.domain.exception.HubTransferInvalidCentralHub;
import com.hubdelivery.hubtohub.domain.exception.HubTransferInvalidHub;
import com.hubdelivery.hubtohub.domain.repository.CentralHubRepository;
import com.hubdelivery.hubtohub.domain.repository.HubToHubWaypointRepository;
import com.hubdelivery.hubtohub.domain.repository.HubTransferRepository;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.response.DirectionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class HubTransferCreateService {

    private final HubRouteService hubRouteService;
    private final HubTransferRepository hubToHubRepository;
    private final HubToHubWaypointRepository waypointRepository;
    private final HubTransferCacheService cacheService;
    private final HubRepository hubRepository;
    private final CentralHubRepository centralHubRepository;

    @Value("${cache.hubToHub.ttl-minutes:60}")
    private long cacheTtlMinutes;

    @Transactional
    public ResGetHubTransferDto createAndSaveNewRoute(HubEntity fromHub, HubEntity toHub) {
        try {
            log.info("카카오 API 응답 시작");
            // 1.  API 호출
            DirectionsResponse directionsResponse = hubRouteService.calculateRoute(fromHub, toHub);
            log.info("카카오 API 응답 받음");

            // 2. DirectionsResponse 파싱
            DirectionsResponse.Summary summary = directionsResponse.getRoutes().get(0).getSummary();

            BigDecimal distanceKm = BigDecimal.valueOf(summary.getDistance())
                    .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            long durationSec = summary.getDuration();

            log.info("경로 정보 파싱 완료 - 거리: {}km, 시간: {}분", distanceKm, (durationSec / 60));


            // 3. DB에 기본 경로 정보 저장
            HubTransferEntity entity = HubTransferEntity.builder()
                    .startHubId(fromHub.getId())
                    .endHubId(toHub.getId())
                    .distance(distanceKm)
                    .durationSec(durationSec)
                    .build();
            HubTransferEntity savedEntity = hubToHubRepository.save(entity);
            log.info("경로 DB 저장 완료 - routeId: {}", savedEntity.getId());

            // 4. 경유지 Entity 생성 및 저장
            List<HubTransferWaypointEntity> waypointEntities = createAndSaveWaypoints(
                    savedEntity.getId(),
                    fromHub,
                    toHub
            );
            log.info("경유지 DB 저장 완료 - 경유지 수: {}", waypointEntities.size());

            // 5. ResponseDto 생성
            ResGetHubTransferDto responseDto = buildResponseDtoFromEntity(
                    savedEntity,
                    waypointEntities
            );

            // 6. Redis에 캐싱
            cacheRoute(responseDto);

            return responseDto;

        } catch (Exception e) {
            log.error("새 경로 생성 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 경유지 Entity 생성 및 저장
     */
    public List<HubTransferWaypointEntity> createAndSaveWaypoints(
            UUID hubToHubId,
            HubEntity fromHub,
            HubEntity toHub) {

        // 1. 중앙허브 조회
        List<HubEntity> centralHubs = hubRouteService.getCentralHubsFromDb();

        // 2. 가장 가까운 중앙허브 찾기
        HubEntity fromCentralHub = hubRouteService.findNearestCentralHub(fromHub, centralHubs);
        HubEntity toCentralHub = hubRouteService.findNearestCentralHub(toHub, centralHubs);

        // 3. 중앙허브 Entity 조회 (Hub ID로)
        CentralHubEntity fromCentralHubEntity = centralHubRepository.findByHubIdIsActive(fromCentralHub.getId())
                .orElseThrow(() -> new IllegalArgumentException("중앙허브를 찾을 수 없습니다: " + fromCentralHub.getId()));

        List<HubTransferWaypointEntity> waypoints = new ArrayList<>();
        int sequence = 1;

        // 4. 출발 중앙허브가 출발지와 다르면 경유지에 추가
        if (!fromCentralHub.getId().equals(fromHub.getId())) {
            HubTransferWaypointEntity fromWaypoint = HubTransferWaypointEntity.builder()
                    .hubToHubId(hubToHubId)
                    .centralHubId(fromCentralHubEntity.getId())
                    .sequence(sequence++)
                    .build();
            waypointRepository.save(fromWaypoint);
            waypoints.add(fromWaypoint);
            log.debug("출발 중앙허브 경유지 추가 - sequence: {}", sequence - 1);
        } else {
            log.debug("출발 중앙허브가 출발지와 동일하므로 경유지에서 제외");
        }

        // 5. 도착 중앙허브가 다르고, 도착지와도 다르면 저장
        if (!fromCentralHub.getId().equals(toCentralHub.getId())
                && !toCentralHub.getId().equals(toHub.getId())) {
            CentralHubEntity toCentralHubEntity = centralHubRepository.findByHubIdIsActive(toCentralHub.getId())
                    .orElseThrow(() -> new HubTransferInvalidCentralHub("존재하지 않는 중앙 허브입니다."));

            HubTransferWaypointEntity toWaypoint = HubTransferWaypointEntity.builder()
                    .hubToHubId(hubToHubId)
                    .centralHubId(toCentralHubEntity.getId())
                    .sequence(sequence++)
                    .build();
            waypointRepository.save(toWaypoint);
            waypoints.add(toWaypoint);
            log.debug("도착 중앙허브 경유지 추가 - sequence: {}", sequence - 1);
        } else {
            if (fromCentralHub.getId().equals(toCentralHub.getId())) {
                log.debug("출발과 도착 중앙허브가 동일하므로 추가 경유지 없음");
            } else {
                log.debug("도착 중앙허브가 도착지와 동일하므로 경유지에서 제외");
            }
        }

        log.info("경유지 생성 완료 - 총 경유지 수: {}", waypoints.size());
        return waypoints;
    }

    /**
     * DB에서 조회한 Entity로 ResponseDto 생성
     */
    public ResGetHubTransferDto buildResponseDtoFromDb(HubTransferEntity entity) {
        // DB에서 경유지 조회
        List<HubTransferWaypointEntity> waypointEntities = waypointRepository
                .findByHubToHubIdAndDeletedAtIsNullOrderBySequence(entity.getId());

        return buildResponseDtoFromEntity(entity, waypointEntities);
    }

    /**
     * ResponseDto 생성 (Entity + Waypoint)
     */
    public ResGetHubTransferDto buildResponseDtoFromEntity(
            HubTransferEntity entity,
            List< HubTransferWaypointEntity > waypointEntities) {

        // Waypoint Entity를 ResponseDto로 변환
        List<ResGetHubTransferDto.WaypointInfo> waypoints = waypointEntities.stream()
                .map(waypoint -> {
                    // CentralHubEntity 조회
                    CentralHubEntity centralHub = centralHubRepository.findById(waypoint.getCentralHubId())
                            .orElseThrow(() -> new HubTransferInvalidCentralHub("존재하지 않는 중앙 허브입니다."));

                    // Hub 정보 조회 (위도, 경도)
                    HubEntity hub = hubRepository.findById(centralHub.getHubId())
                            .orElseThrow(() -> new HubTransferInvalidHub("존재하지 않는 허브입니다."));

                    return ResGetHubTransferDto.WaypointInfo.builder()
                            .name(hub.getHubName())
                            .latitude(hub.getLatitude())
                            .longitude(hub.getLongitude())
                            .sequence(waypoint.getSequence())
                            .build();
                })
                .toList();

        return ResGetHubTransferDto.builder()
                .routeId(entity.getId())
                .startHubId(entity.getStartHubId())
                .endHubId(entity.getEndHubId())
                .distanceKm(entity.getDistance())
                .durationMinutes((int) (entity.getDurationSec() / 60))
                .durationSec(entity.getDurationSec())
                .waypoints(waypoints)
                .build();
    }

    /**
     * ResponseDto를 Redis에 캐싱
     */
    public void cacheRoute(ResGetHubTransferDto responseDto) {
        try {
            HubTransferCacheData cacheData = HubTransferCacheData.fromResponseDto(responseDto);
            cacheService.cacheRoute(
                    responseDto.getStartHubId(),
                    responseDto.getEndHubId(),
                    cacheData,
                    cacheTtlMinutes
            );
            log.info("경로 캐시 저장 완료");
        } catch (Exception e) {
            log.warn("경로 캐시 저장 실패 (계속 진행)", e);
        }
    }

    /**
     * transferId로 캐시에 저장
     */
    public void cacheRouteByTransferId(UUID transferId, ResGetHubTransferDto responseDto) {
        try {
            HubTransferCacheData cacheData = HubTransferCacheData.fromResponseDto(responseDto);
            cacheService.cacheRouteByTransferId(transferId, cacheData, cacheTtlMinutes);
            log.info("경로 캐시 저장 완료 - transferId: {}", transferId);
        } catch (Exception e) {
            log.warn("경로 캐시 저장 실패 (계속 진행)", e);
        }
    }

    public void cacheTransfer(UUID transferId, ResGetHubTransferDto responseDto) {
        try {
            HubTransferCacheData cacheData = HubTransferCacheData.fromResponseDto(responseDto);

            // transferId로 캐시 저장
            cacheService.cacheRouteByTransferId(transferId, cacheData, cacheTtlMinutes);

            // hubId 조합으로도 캐시 저장
            cacheService.cacheRoute(responseDto.getStartHubId(), responseDto.getEndHubId(), cacheData, cacheTtlMinutes);

            log.info("경로 캐시 갱신 완료");
        } catch (Exception e) {
            log.warn("경로 캐시 갱신 실패 (계속 진행)", e);
        }
    }


}
