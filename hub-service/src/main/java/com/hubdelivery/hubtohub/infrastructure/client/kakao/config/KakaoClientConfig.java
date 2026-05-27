package com.hubdelivery.hubtohub.infrastructure.client.kakao.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class KakaoClientConfig {

    @Value("${kakao.mobility.api-key}")
    private String apiKey;

    @Value("${kakao.mobility.base-url}")
    private String baseUrl;

    @Bean
    public RestClient kakaoMobilityRestClient(){
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION,
                        "KakaoAK "+apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
