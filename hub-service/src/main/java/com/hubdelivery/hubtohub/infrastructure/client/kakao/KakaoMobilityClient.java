package com.hubdelivery.hubtohub.infrastructure.client.kakao;


import com.hubdelivery.hubtohub.infrastructure.client.kakao.response.DirectionsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoMobilityClient {

    private final RestClient kakaoMobilityRestClient;

    public DirectionsResponse getDirections(
            Coordinate origin,
            Coordinate destination,
            List<Coordinate> waypoints
    ) {
        String originParam = toParam(origin);
        String destParam = toParam(destination);

        try {
            String rawResponse = kakaoMobilityRestClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder
                                .path("/v1/directions")
                                .queryParam("origin", originParam)
                                .queryParam("destination", destParam)
                                .queryParam("priority", "RECOMMEND");

                        // 경유지 추가
                        if (waypoints != null && !waypoints.isEmpty()) {
                            String waypointsParam = waypoints.stream()
                                    .map(this::toParam)
                                    .collect(Collectors.joining("|"));
                            uriBuilder.queryParam("waypoints", waypointsParam);
                        }

                        return uriBuilder.build();
                    })
                    .retrieve()
                    .body(String.class);


            // 객체로 변환
            return convertToResponse(rawResponse);

        } catch (RestClientResponseException e) {
            log.error("카카오 API 호출 실패 - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("카카오 길찾기 API 호출 실패", e);
        } catch (Exception e) {
            log.error("카카오 API 예상치 못한 에러", e);
            throw new RuntimeException("카카오 길찾기 API 에러", e);
        }
    }

    /**
     * "경도,위도" 형식으로 변환
     */
    private String toParam(Coordinate coord) {
        return coord.longitude() + "," + coord.latitude();
    }

    private DirectionsResponse convertToResponse(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, DirectionsResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("응답 파싱 실패: " + json, e);
        }
    }

    public record Coordinate(BigDecimal latitude, BigDecimal longitude){}

}
