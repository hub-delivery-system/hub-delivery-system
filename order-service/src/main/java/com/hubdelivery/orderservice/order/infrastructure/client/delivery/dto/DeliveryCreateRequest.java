package com.hubdelivery.orderservice.order.infrastructure.client.delivery.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeliveryCreateRequest {

    private UUID orderId;
    private UUID startHubId;
    private UUID endHubId;
    private String address;
    private UUID userId;       // 배송 수령인 userId
    private String slackId;
    private UUID deliveryManagerId; // 수동 지정 시 사용 (선택)
    private List<RouteRequest> routes;

    @Getter
    @Builder
    public static class RouteRequest {
        private Integer sequence;
        private UUID startHubId;
        private UUID endHubId;
        private BigDecimal estimatedDistance;
        private Integer estimatedDuration;
        private UUID deliveryManagerId; // 수동 지정 시 사용 (선택)
    }
}
