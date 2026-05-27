package com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.dto.UserResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    // TODO: user-service에 해당 엔드포인트 추가 후 HUB_MANAGER 담당 허브 검증에 활용
    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable UUID userId);
}
