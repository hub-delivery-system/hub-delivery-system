package com.hubdelivery.common.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private UUID orderId;
    private UUID startHubId;
    private UUID endHubId;
    private String address;
    private UUID userId;
    private String slackId;
    private UUID deliveryManagerId;
    private List<RouteInfo> routes;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RouteInfo {
        private Integer sequence;
        private UUID startHubId;
        private UUID endHubId;
        private BigDecimal estimatedDistance;
        private Integer estimatedDuration;
        private UUID deliveryManagerId;
    }
}
