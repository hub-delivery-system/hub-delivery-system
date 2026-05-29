package com.hubdelivery.slack.application.service;

import com.hubdelivery.slack.infrastructure.client.dto.CompanyDeliveryManagerDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NaverDirectionsService {

    @Value("${naver.directions.client-id}")
    private String clientId;

    @Value("${naver.directions.client-secret}")
    private String clientSecret;

    @Value("${naver.directions.base-url}")
    private String baseUrl;

    // ✅ naverRestTemplate Bean을 명시적으로 주입
    private final RestTemplate restTemplate;

    public NaverDirectionsService(@Qualifier("naverRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Circuit Breaker: "naver"
     * - Naver API 장애 시 빈 경로(empty) 반환 → 스케줄러는 경로 정보 없이 계속 진행
     */
    @CircuitBreaker(name = "naver", fallbackMethod = "getRouteFallback")
    public NaverRouteResult getOptimalRoute(
            List<CompanyDeliveryManagerDto.DeliveryDestinationDto> destinations) {

        if (destinations == null || destinations.size() < 2) {
            return NaverRouteResult.empty();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        String start = formatCoord(destinations.get(0));
        String goal  = formatCoord(destinations.get(destinations.size() - 1));

        String waypoints = destinations.stream()
                .skip(1).limit(destinations.size() - 2)
                .map(this::formatCoord)
                .collect(Collectors.joining("|"));

        String url = baseUrl + "/driving"
                + "?start=" + start
                + "&goal=" + goal
                + (waypoints.isEmpty() ? "" : "&waypoints=" + waypoints)
                + "&option=trafast";

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        return extractRouteResult(response.getBody(), destinations);
    }

    @SuppressWarnings("unused")
    private NaverRouteResult getRouteFallback(
            List<CompanyDeliveryManagerDto.DeliveryDestinationDto> destinations, Throwable t) {
        log.warn("[Naver CircuitBreaker] fallback 실행 - 원인: {}", t.getMessage());
        // empty() 반환 → buildFinalMessage에서 경로 정보 블록만 빠지고 나머지는 정상 발송
        return NaverRouteResult.empty();
    }

    private String formatCoord(CompanyDeliveryManagerDto.DeliveryDestinationDto dest) {
        return dest.getLongitude() + "," + dest.getLatitude();
    }

    @SuppressWarnings("unchecked")
    private NaverRouteResult extractRouteResult(Map<?, ?> body,
                                                List<CompanyDeliveryManagerDto.DeliveryDestinationDto> destinations) {
        try {
            Map<?, ?> route   = (Map<?, ?>) body.get("route");
            List<?>   trafast = (List<?>) route.get("trafast");
            Map<?, ?> summary = (Map<?, ?>) ((Map<?, ?>) trafast.get(0)).get("summary");

            int duration = (int) summary.get("duration");
            int distance = (int) summary.get("distance");

            return NaverRouteResult.builder()
                    .durationSeconds(duration / 1000)
                    .distanceMeters(distance)
                    .destinations(destinations)
                    .build();
        } catch (Exception e) {
            log.error("네이버 응답 파싱 실패: {}", e.getMessage());
            return NaverRouteResult.empty();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NaverRouteResult {
        private int durationSeconds;
        private int distanceMeters;
        private List<CompanyDeliveryManagerDto.DeliveryDestinationDto> destinations;

        public static NaverRouteResult empty() {
            return NaverRouteResult.builder()
                    .durationSeconds(0).distanceMeters(0).destinations(List.of()).build();
        }

        public boolean isEmpty() {
            return destinations == null || destinations.isEmpty();
        }
    }
}