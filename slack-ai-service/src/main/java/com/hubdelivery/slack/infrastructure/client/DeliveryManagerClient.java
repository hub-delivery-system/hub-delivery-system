package com.hubdelivery.slack.infrastructure.client;

import com.hubdelivery.slack.infrastructure.client.dto.CompanyDeliveryManagerDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "delivery-service", path = "/api/v1")
public interface DeliveryManagerClient {

    // 업체 배송담당자 목록 조회
    @GetMapping("/delivery-managers")
    List<CompanyDeliveryManagerDto> getCompanyDeliveryManagers(
            @RequestParam(value = "type") String type,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Role") String role
    );
}
