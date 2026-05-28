package com.hubdelivery.orderservice.order.infrastructure.client.product;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.orderservice.order.infrastructure.client.product.dto.ProductResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "COMPANY-PRODUCT-SERVICE")
public interface ProductServiceClient {

    // 상품 정보 조회 (HUB_MANAGER 권한 체크 시 hubId 확인 용도)
    @GetMapping("/api/v1/products/{productId}")
    ApiResponse<ProductResponse> getProduct(@PathVariable UUID productId);
}
