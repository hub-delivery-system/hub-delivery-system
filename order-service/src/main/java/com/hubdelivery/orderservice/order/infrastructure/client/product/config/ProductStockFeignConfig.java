package com.hubdelivery.orderservice.order.infrastructure.client.product.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ProductStockFeignConfig {

    // 재고 감소/증가는 내부 서비스 호출이므로 MASTER 권한으로 고정
    // DELIVERY_MANAGER 등 제한된 권한 사용자가 주문을 생성해도 재고 API 호출 가능하도록 처리
    @Bean
    public RequestInterceptor productStockRequestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            String userId = "system";
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String headerUserId = request.getHeader("X-User-Id");
                if (headerUserId != null) {
                    userId = headerUserId;
                }
            }

            template.header("X-User-Id", userId);
            template.header("X-Role", "MASTER");
        };
    }
}
