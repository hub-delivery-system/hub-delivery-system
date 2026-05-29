package com.hubdelivery.slack.application.service;

import com.hubdelivery.slack.domain.exception.SlackErrorCode;
import com.hubdelivery.slack.domain.exception.SlackException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${ai.gemini.api-key}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=";

    private final RestTemplate restTemplate;

    public String generateDeadlineMessage(String prompt) {
        try {
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

        } catch (Exception e) {
            log.error("Gemini API 호출 실패: {}", e.getMessage());
            // 수정: CommonException(SlackErrorCode) → SlackException(SlackErrorCode)
            // CommonException은 CommonErrorCode만 받으므로 SlackException 사용
            throw new SlackException(SlackErrorCode.AI_API_CALL_FAILED);
        }
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