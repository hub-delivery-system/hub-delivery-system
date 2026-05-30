package com.hubdelivery.slack.application.service;

import com.hubdelivery.slack.infrastructure.client.dto.CompanyDeliveryManagerDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Circuit Breaker Fallback 테스트
 *
 * 목적:
 * - 외부 API 장애 또는 Circuit OPEN 상황에서 실행될 fallback 메서드의 반환값을 검증한다.
 *
 * 주의:
 * - 이 테스트는 Resilience4j 프록시를 실제로 띄우는 통합 테스트가 아니다.
 * - DB, Eureka, Kafka 없이 빠르게 검증하기 위해 fallback 메서드 자체를 ReflectionTestUtils로 호출한다.
 * - 실제 CircuitBreaker 상태 전이는 actuator/로그 또는 별도 통합 테스트로 검증한다.
 */
class CircuitBreakerFallbackTest {

    @Test
    @DisplayName("Gemini fallback은 null을 반환한다")
    void geminiFallback_returnsNull() {
        // given
        GeminiService geminiService = new GeminiService(mock(RestTemplate.class));

        // when
        String result = ReflectionTestUtils.invokeMethod(
                geminiService,
                "generateFallback",
                "prompt",
                new RuntimeException("Gemini API 장애")
        );

        // then
        assertNull(result);
    }

    @Test
    @DisplayName("Naver Directions fallback은 empty route result를 반환한다")
    void naverFallback_returnsEmptyRoute() {
        // given
        NaverDirectionsService naverDirectionsService =
                new NaverDirectionsService(mock(RestTemplate.class));

        List<CompanyDeliveryManagerDto.DeliveryDestinationDto> destinations = List.of(
                CompanyDeliveryManagerDto.DeliveryDestinationDto.builder()
                        .deliveryId(UUID.randomUUID())
                        .address("서울")
                        .latitude(37.5665)
                        .longitude(126.9780)
                        .receiverName("수령업체A")
                        .build(),
                CompanyDeliveryManagerDto.DeliveryDestinationDto.builder()
                        .deliveryId(UUID.randomUUID())
                        .address("성남")
                        .latitude(37.4200)
                        .longitude(127.1265)
                        .receiverName("수령업체B")
                        .build()
        );

        // when
        NaverDirectionsService.NaverRouteResult result = ReflectionTestUtils.invokeMethod(
                naverDirectionsService,
                "getRouteFallback",
                destinations,
                new RuntimeException("Naver Directions API 장애")
        );

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getDistanceMeters());
        assertEquals(0, result.getDurationSeconds());
    }

    @Test
    @DisplayName("Slack fallback은 false를 반환한다")
    void slackFallback_returnsFalse() {
        // given
        SlackSendService slackSendService = new SlackSendService();

        // when
        Boolean result = ReflectionTestUtils.invokeMethod(
                slackSendService,
                "sendMessageFallback",
                "U123456",
                "테스트 메시지",
                new RuntimeException("Slack API 장애")
        );

        // then
        assertFalse(result);
    }
}
