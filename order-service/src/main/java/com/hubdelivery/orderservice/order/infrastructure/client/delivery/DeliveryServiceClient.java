package com.hubdelivery.orderservice.order.infrastructure.client.delivery;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.orderservice.global.config.InternalFeignConfig;
import com.hubdelivery.orderservice.order.infrastructure.client.delivery.dto.DeliveryCreateRequest;
import com.hubdelivery.orderservice.order.infrastructure.client.delivery.dto.DeliveryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// InternalFeignConfig로 X-Role=MASTER 고정 (delivery-service create 엔드포인트 MASTER 전용)
@FeignClient(name = "DELIVERY-SERVICE", configuration = InternalFeignConfig.class)
public interface DeliveryServiceClient {

    @PostMapping("/api/v1/deliveries")
    ApiResponse<DeliveryResponse> create(@RequestBody DeliveryCreateRequest request);
}
