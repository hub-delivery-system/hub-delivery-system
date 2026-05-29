package com.hubdelivery.company.global.infrastructure.client;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.global.infrastructure.client.config.UserClientConfig;
import com.hubdelivery.company.global.infrastructure.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1/users", configuration = UserClientConfig.class)
public interface UserClient {

    @GetMapping("/{user_id}")
    ApiResponse<UserResponse> getUser(
            @RequestHeader("X-User-Id") UUID requestUserId,
            @RequestHeader("X-Role") UserRole requestUserRole,
            @PathVariable("user_id") UUID userId
    );
}
