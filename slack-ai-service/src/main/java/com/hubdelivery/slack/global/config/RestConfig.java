package com.hubdelivery.slack.global.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestConfig {

    /**
     * Gemini API용 RestTemplate
     * - Gemini는 AI 추론 시간이 있어서 read timeout을 넉넉하게 10초로 설정
     * - connect timeout은 짧게 3초 (연결 자체가 안 되면 빠르게 실패)
     */
    @Bean("geminiRestTemplate")
    public RestTemplate geminiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Naver Directions API용 RestTemplate
     * - 지도 API는 응답이 빠르므로 양쪽 모두 짧게
     */

    @Bean("naverRestTemplate")
    public RestTemplate naverRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

}