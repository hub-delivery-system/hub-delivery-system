package com.hubdelivery.company.global.infrastructure.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.company.global.infrastructure.client.config.HubClientConfig;
import com.hubdelivery.company.global.infrastructure.client.dto.HubResponse;

@FeignClient(name = "hub-service", path = "/api/v1/hubs", configuration = HubClientConfig.class)
public interface HubClient {

    @GetMapping("/{hub_id}")
    ApiResponse<HubResponse> getHub(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-Role") UserRole userRole,
            @PathVariable("hub_id") UUID hubId
    );
}
