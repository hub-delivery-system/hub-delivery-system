package com.hubdelivery.company.global.infrastructure.client.user;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.global.infrastructure.client.user.config.UserClientConfig;
import com.hubdelivery.company.global.infrastructure.client.user.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

// user-service는 현재 Eureka에 등록하지 않으므로 URL 기반으로 직접 호출
@FeignClient(
        name = "user-service",
        url = "${integration.user-service.base-url}",
        path = "/api/v1/users",
        configuration = UserClientConfig.class
)
public interface UserClient {

    @GetMapping("/{user_id}")
    ApiResponse<UserResponse> getUser(
            @RequestHeader("X-User-Id") UUID requestUserId,
            @RequestHeader("X-Role") UserRole requestUserRole,
            @PathVariable("user_id") UUID userId
    );
}
