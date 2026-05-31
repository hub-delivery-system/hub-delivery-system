package com.hubdelivery.orderservice.order.infrastructure.client.product;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.orderservice.order.infrastructure.client.product.config.ProductStockFeignConfig;
import com.hubdelivery.orderservice.order.infrastructure.client.product.dto.ProductResponse;
import com.hubdelivery.orderservice.order.infrastructure.client.product.dto.ProductStockRequest;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "COMPANY-PRODUCT-SERVICE", contextId = "productStockClient",
        configuration = ProductStockFeignConfig.class)
public interface ProductStockClient {

    @PatchMapping("/api/v1/products/{productId}/stock/decrease")
    ApiResponse<ProductResponse> decreaseStock(
            @PathVariable UUID productId,
            @RequestBody ProductStockRequest request);

    @PatchMapping("/api/v1/products/{productId}/stock/increase")
    ApiResponse<ProductResponse> increaseStock(
            @PathVariable UUID productId,
            @RequestBody ProductStockRequest request);
}
