package com.hubdelivery.orderservice.order.infrastructure.client.user;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.orderservice.order.infrastructure.client.user.dto.UserResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE")
public interface UserServiceClient {

    // HUB_MANAGER 권한 체크 시 사용자의 담당 hubId 조회
    @GetMapping("/api/v1/users/{userId}")
    ApiResponse<UserResponse> getUser(@PathVariable UUID userId);
}
