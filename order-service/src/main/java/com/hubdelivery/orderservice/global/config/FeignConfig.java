package com.hubdelivery.orderservice.global.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    // Gateway가 전달한 X-User-Id, X-Role 헤더를 Feign 요청에 그대로 전파
    // delivery-service 등 내부 서비스 호출 시 인증 컨텍스트 유지에 필요
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                template.header("X-User-Id", request.getHeader("X-User-Id"));
                template.header("X-Role", request.getHeader("X-Role"));
            }
        };
    }
}
