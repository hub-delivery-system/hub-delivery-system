package com.hubdelivery.slack.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RestConfig 테스트
 *
 * 목적:
 * - Gemini API용 RestTemplate Bean 생성 검증
 * - Naver Directions API용 RestTemplate Bean 생성 검증
 * - Config 설정이 정상적으로 적용되는지 확인
 *
 * 검증 내용:
 * - Bean 생성 시 null이 아닌 객체 반환
 * - Spring Container 등록 전 기본 생성 동작 확인
 */
class RestConfigTest {

    private final RestConfig restConfig = new RestConfig();

    /**
     * Gemini API용 RestTemplate 생성 테스트
     *
     * 목적:
     * - Gemini API 호출에 사용할 RestTemplate이 정상 생성되는지 확인
     * - connect timeout, read timeout 설정 객체 생성 검증
     */
    @Test
    void geminiRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder();

        RestTemplate restTemplate =
                restConfig.geminiRestTemplate(builder);

        assertNotNull(restTemplate);
    }

    /**
     * Naver Directions API용 RestTemplate 생성 테스트
     *
     * 목적:
     * - Naver 길찾기 API 호출용 RestTemplate 정상 생성 확인
     * - API 호출 객체 생성 실패 여부 검증
     */
    @Test
    void naverRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder();

        RestTemplate restTemplate =
                restConfig.naverRestTemplate(builder);

        assertNotNull(restTemplate);
    }
}