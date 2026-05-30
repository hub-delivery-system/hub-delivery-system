package com.hubdelivery.slack.application.service;

import com.hubdelivery.slack.domain.exception.SlackException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GeminiService 테스트
 *
 * 목적:
 * - Gemini API 응답을 정상적으로 파싱하는지 검증한다.
 * - Gemini 응답 구조가 깨졌을 때 SlackException으로 처리되는지 검증한다.
 *
 * 테스트 방식:
 * - 실제 Gemini API를 호출하지 않는다.
 * - RestTemplate을 Mock 처리한다.
 */
class GeminiServiceTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final GeminiService geminiService = new GeminiService(restTemplate);

    @Test
    @DisplayName("Gemini API 응답에서 text 값을 추출한다")
    void generateDeadlineMessage_success() {
        // given
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");

        Map<String, Object> responseBody = Map.of(
                "candidates", List.of(
                        Map.of(
                                "content", Map.of(
                                        "parts", List.of(
                                                Map.of("text", "12월 10일 오전 9시까지 발송해야 합니다.")
                                        )
                                )
                        )
                )
        );

        when(restTemplate.exchange(
                contains("https://generativelanguage.googleapis.com"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(responseBody));

        // when
        String result = geminiService.generateDeadlineMessage("배송 시한 알려줘");

        // then
        assertEquals("12월 10일 오전 9시까지 발송해야 합니다.", result);

        verify(restTemplate, times(1)).exchange(
                contains("https://generativelanguage.googleapis.com"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    @DisplayName("Gemini 응답 파싱 실패 시 SlackException을 던진다")
    void generateDeadlineMessage_parseFail() {
        // given
        ReflectionTestUtils.setField(geminiService, "apiKey", "test-api-key");

        Map<String, Object> invalidResponseBody = Map.of(
                "invalid", "response"
        );

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(invalidResponseBody));

        // when & then
        assertThrows(SlackException.class,
                () -> geminiService.generateDeadlineMessage("배송 시한 알려줘"));
    }
}
