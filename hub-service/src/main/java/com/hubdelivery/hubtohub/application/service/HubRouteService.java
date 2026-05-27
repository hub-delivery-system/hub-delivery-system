package com.hubdelivery.hubtohub.application.service;

import com.hubdelivery.hub.domain.entity.HubEntity;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.KakaoMobilityClient;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.KakaoMobilityClient.Coordinate;
import com.hubdelivery.hubtohub.infrastructure.client.kakao.response.DirectionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HubRouteService {

    private final KakaoMobilityClient kakaoClient;

    // 중앙허브 좌표 정의
    private static final Coordinate GYEONGGI_NAMBU_HUB =
            new Coordinate(new BigDecimal("37.270000"), new BigDecimal("127.010000"));
    private static final Coordinate DAEJEON_HUB =
            new Coordinate(new BigDecimal("36.350000"), new BigDecimal("127.380000"));
    private static final Coordinate DAEGU_HUB =
            new Coordinate(new BigDecimal("35.870000"), new BigDecimal("128.600000"));

    /**
     * 두 허브 간 경로 계산 (카카오 API + Hub and Spoke)
     */
    public RouteInfo calculateRoute(HubEntity fromHub, HubEntity toHub) {
        log.info("경로 계산 시작 - from: {} → to: {}",
                fromHub.getHubName(), toHub.getHubName());

        // 1. 각 허브의 가장 가까운 중앙허브 찾기
        Coordinate fromCentralHub = findNearestCentralHub(fromHub);
        Coordinate toCentralHub = findNearestCentralHub(toHub);

        // 2. 경유지 구성
        List<Coordinate> waypoints = buildWaypoints(fromCentralHub, toCentralHub);

        // 3. 카카오 API 호출
        Coordinate origin = new Coordinate(fromHub.getLatitude(), fromHub.getLongitude());
        Coordinate destination = new Coordinate(toHub.getLatitude(), toHub.getLongitude());

        DirectionsResponse response = kakaoClient.getDirections(
                origin, destination, waypoints
        );
        DirectionsResponse.Route route = response.getRoutes().get(0);
        List<DirectionsResponse.Section> sections = route.getSections();

        // 4. 결과 추출
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

        return new RouteInfo(distanceKm, durationMinutes);
    }

    /**
     * 가장 가까운 중앙허브 찾기 (Haversine)
     */
    private Coordinate findNearestCentralHub(HubEntity hub) {
        List<Coordinate> centralHubs = List.of(
                GYEONGGI_NAMBU_HUB, DAEJEON_HUB, DAEGU_HUB
        );

        Coordinate hubCoord = new Coordinate(hub.getLatitude(), hub.getLongitude());

        return centralHubs.stream()
                .min((a, b) -> Double.compare(
                        distance(hubCoord, a),
                        distance(hubCoord, b)
                ))
                .orElseThrow();
    }

    /**
     * Hub and Spoke 경유지 구성
     */
    private List<Coordinate> buildWaypoints(Coordinate from, Coordinate to) {
        // ⭐ 좌표를 읽어서 한글 이름으로 변환한 뒤 로그 출력
        String fromName = getCentralHubName(from);
        String toName = getCentralHubName(to);

        log.info("Hub & Spoke 경유지 구성 - 출발지 중앙허브: [{}], 도착지 중앙허브: [{}]", fromName, toName);

        if (from.equals(to)) {
            // 같은 중앙허브 소속이면 해당 중앙허브 한 번만 경유
            log.info("동일 권역 내 이동: 경유지 1개 [{}]", fromName);
            return List.of(from);
        }

        // 다른 중앙허브: 출발 중앙허브 → 도착 중앙허브
        log.info("타 권역 간 이동: 경유지 2개 [{} → {}]", fromName, toName);
        return List.of(from, to);
    }

    private String getCentralHubName(Coordinate coordinate) {
        if (GYEONGGI_NAMBU_HUB.equals(coordinate)) {
            return "경기남부 중앙허브";
        } else if (DAEJEON_HUB.equals(coordinate)) {
            return "대전 중앙허브";
        } else if (DAEGU_HUB.equals(coordinate)) {
            return "대구 중앙허브";
        }
        return "미정의 중앙허브";
    }

    /**
     * Haversine으로 중앙허브 선택용 거리 계산
     */
    private double distance(Coordinate a, Coordinate b) {
        double lat1 = a.latitude().doubleValue();
        double lon1 = a.longitude().doubleValue();
        double lat2 = b.latitude().doubleValue();
        double lon2 = b.longitude().doubleValue();

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double aa = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371 * 2 * Math.atan2(Math.sqrt(aa), Math.sqrt(1 - aa));
    }

    public record RouteInfo(BigDecimal distanceKm, Integer durationMinutes) {}
}
