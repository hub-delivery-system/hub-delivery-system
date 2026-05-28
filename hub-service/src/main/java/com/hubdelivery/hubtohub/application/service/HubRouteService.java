package com.hubdelivery.hubtohub.application.service;

import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hub.domain.repository.HubRepository;
import com.hubdelivery.hubtohub.domain.entity.CentralHubEntity;
import com.hubdelivery.hubtohub.domain.exception.KakaoApiException;
import com.hubdelivery.hubtohub.domain.exception.KakaoRouteNotFoundException;
import com.hubdelivery.hubtohub.domain.repository.CentralHubRepository;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.KakaoMobilityClient;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.KakaoMobilityClient.Coordinate;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.response.DirectionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HubRouteService {

    private final KakaoMobilityClient kakaoClient;
    private final CentralHubRepository centralHubRepository;
    private final HubRepository hubRepository;

    /**
     * 두 허브 간 경로 계산 (카카오 API + Hub and Spoke)
     */
    public DirectionsResponse calculateRoute(HubEntity fromHub, HubEntity toHub) {
        try {
            log.info("경로 계산 시작 - from: {} → to: {}",
                    fromHub.getHubName(), toHub.getHubName());

            // 1. DB에서 중앙허브 조회
            List<HubEntity> centralHubs = getCentralHubsFromDb_Simple();

            // 2. 각 허브의 가장 가까운 중앙허브 찾기
            HubEntity fromCentralHub = findNearestCentralHub(fromHub, centralHubs);
            HubEntity toCentralHub = findNearestCentralHub(toHub, centralHubs);

            // 3. 경유지 구성 (중앙허브의 좌표 사용)
            List<Coordinate> waypoints = buildWaypoints(fromCentralHub, toCentralHub);

            // 4. 카카오 API 호출
            Coordinate origin = new Coordinate(fromHub.getLatitude(), fromHub.getLongitude());
            Coordinate destination = new Coordinate(toHub.getLatitude(), toHub.getLongitude());

            DirectionsResponse response = kakaoClient.getDirections(
                    origin, destination, waypoints
            );

            DirectionsResponse.Route route = response.getRoutes().get(0);
            if (route.getResultCode() != 0) {
                log.warn("카카오 길찾기 실패 - resultCode: {}, msg: {}",
                        route.getResultCode(), route.getResultMsg());
                throw new KakaoRouteNotFoundException();
            }

            List<DirectionsResponse.Section> sections = route.getSections();

            // 5. 결과 추출
            DirectionsResponse.Summary summary = route.getSummary();

            for (int i = 0; i < sections.size(); i++) {
                DirectionsResponse.Section section = sections.get(i);
                double distanceKm = section.getDistance() / 1000.0;
                int durationMin = section.getDuration() / 60;

                log.info("구간 {}: {}km, {}분", i + 1, distanceKm, durationMin);
            }

            BigDecimal distanceKm = BigDecimal.valueOf(summary.getDistance())
                    .divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
            int durationMinutes = summary.getDuration() / 60;

            log.info("경로 계산 완료 - 거리: {}km, 시간: {}분", distanceKm, durationMinutes);

            return response;
        } catch (RestClientResponseException e) {
            log.error("카카오 API 호출 실패", e);
            throw new KakaoApiException();
        } catch (Exception e) {
            log.error("카카오 응답 파싱 실패", e);
            throw new KakaoApiException();
        }
    }

    /**
     * DB에서 중앙허브 조회
     */
    @Transactional(readOnly = true)
    public List<HubEntity> getCentralHubsFromDb_Simple() {
        return centralHubRepository.findAll().stream()
                .map(CentralHubEntity::getHub)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 가장 가까운 중앙허브 찾기 (Haversine)
     */
    public HubEntity findNearestCentralHub(HubEntity hub, List<HubEntity> centralHubs) {
        return centralHubs.stream()
                .min((a, b) -> Double.compare(
                        distance(hub, a),
                        distance(hub, b)
                ))
                .orElseThrow();
    }

    /**
     * Hub and Spoke 경유지 구성
     */
    private List<Coordinate> buildWaypoints(HubEntity from, HubEntity to) {
        String fromName = from.getHubName();
        String toName = to.getHubName();

        log.info("Hub & Spoke 경유지 구성 - 출발지 중앙허브: [{}], 도착지 중앙허브: [{}]", fromName, toName);

        // 출발지와 도착지가 같은 중앙허브 소속
        if (from.getId().equals(to.getId())) {
            log.info("동일 권역 내 이동: 경유지 없음 [{}]", fromName);
            return List.of();  // 경유지 없음
        }

        // 서로 다른 중앙허브
        Coordinate fromCoord = new Coordinate(from.getLatitude(), from.getLongitude());
        Coordinate toCoord = new Coordinate(to.getLatitude(), to.getLongitude());

        log.info("타 권역 간 이동: 경유지 2개 [{} → {}]", fromName, toName);
        return List.of(fromCoord, toCoord);
    }

    /**
     * Haversine으로 거리 계산
     */
    public double distance(HubEntity a, HubEntity b) {
        double lat1 = a.getLatitude().doubleValue();
        double lon1 = a.getLongitude().doubleValue();
        double lat2 = b.getLatitude().doubleValue();
        double lon2 = b.getLongitude().doubleValue();

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double aa = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(aa), Math.sqrt(1 - aa));
    }

    public record RouteInfo(BigDecimal distanceKm, Integer durationMinutes) {}
}
