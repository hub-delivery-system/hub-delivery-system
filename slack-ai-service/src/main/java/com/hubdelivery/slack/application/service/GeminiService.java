package com.hubdelivery.slack.application.service;

import com.hubdelivery.slack.domain.exception.SlackErrorCode;
import com.hubdelivery.slack.domain.exception.SlackException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";

    // ✅ geminiRestTemplate Bean을 명시적으로 주입
    private final RestTemplate restTemplate;

    public GeminiService(@Qualifier("geminiRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Circuit Breaker: "gemini"
     *
     * - CLOSED  → 정상 호출
     * - OPEN    → fallback 즉시 반환 (Gemini 호출 안 함)
     * - HALF_OPEN → 일부 요청만 통과시켜 복구 여부 확인
     *
     * fallbackMethod가 실행되는 조건:
     *   1. Gemini가 예외를 던질 때 (HTTP 5xx, 타임아웃 등)
     *   2. Circuit이 OPEN 상태일 때
     */
    @CircuitBreaker(name = "gemini", fallbackMethod = "generateFallback")
    public String generateDeadlineMessage(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        ResponseEntity<Map> response = restTemplate.exchange(
                GEMINI_URL + apiKey,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Map.class
        );

        return extractText(response.getBody());
    }

    /**
     * Fallback: Gemini 호출 실패 또는 Circuit OPEN 시 실행
     * - 스케줄러에서 이미 try-catch로 감싸고 있지만,
     *   Circuit이 OPEN이면 예외 자체를 던지지 않고 여기서 처리
     */
    @SuppressWarnings("unused")
    private String generateFallback(String prompt, Throwable t) {
        log.warn("[Gemini CircuitBreaker] fallback 실행 - 원인: {}", t.getMessage());
        // null 반환 → 호출부(AiMessageService, DeliveryAlertScheduler)에서
        // "SUCCESS" 판정이 안 되어 자동으로 fallback 메시지 사용됨
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> responseBody) {
        try {
            List<Map<?, ?>> candidates = (List<Map<?, ?>>) responseBody.get("candidates");
            Map<?, ?> content          = (Map<?, ?>) candidates.get(0).get("content");
            List<Map<?, ?>> parts      = (List<Map<?, ?>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패: {}", e.getMessage());
            throw new SlackException(SlackErrorCode.AI_API_CALL_FAILED);
        }
    }
}