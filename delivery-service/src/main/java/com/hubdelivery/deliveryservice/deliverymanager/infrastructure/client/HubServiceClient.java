package com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client;

import com.hubdelivery.common.response.ApiResponse;
import com.hubdelivery.deliveryservice.deliverymanager.infrastructure.client.dto.HubResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "HUB-SERVICE")
public interface HubServiceClient {

    @GetMapping("/api/v1/hubs/{hubId}")
    ApiResponse<HubResponse> getHub(@PathVariable UUID hubId);
}
