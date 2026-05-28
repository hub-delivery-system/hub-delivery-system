package com.hubdelivery.orderservice.global.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * 내부 서비스 간 Feign 호출용 설정.
 * delivery-service의 create 엔드포인트는 MASTER 권한을 요구하므로
 * order-service가 내부적으로 호출할 때 X-Role을 MASTER로 고정한다.
 *
 * @Configuration 을 붙이지 않아 전역 빈으로 등록되지 않으며,
 * @FeignClient(configuration = ...) 로 명시된 클라이언트에만 적용된다.
 */
public class InternalFeignConfig {

    @Bean
    public RequestInterceptor internalRequestInterceptor() {
        return template -> {
            template.header("X-User-Id", "order-service");
            template.header("X-Role", "MASTER");
        };
    }
}
