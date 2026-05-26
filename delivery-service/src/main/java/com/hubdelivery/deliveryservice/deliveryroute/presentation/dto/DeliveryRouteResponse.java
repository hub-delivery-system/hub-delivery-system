package com.hubdelivery.deliveryservice.deliveryroute.presentation.dto;

import com.hubdelivery.deliveryservice.deliveryroute.domain.entity.DeliveryRoute;
import com.hubdelivery.deliveryservice.deliveryroute.domain.type.DeliveryRouteStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class DeliveryRouteResponse {

    private final UUID id;
    private final UUID deliveryId;
    private final Integer sequence;
    private final UUID startHubId;
    private final UUID endHubId;
    private final BigDecimal estimatedDistance;
    private final Integer estimatedDuration;
    private final BigDecimal realDistance;
    private final Integer realDuration;
    private final DeliveryRouteStatus status;
    private final UUID deliveryManagerId;
    private final LocalDateTime createdAt;

    private DeliveryRouteResponse(DeliveryRoute route) {
        this.id = route.getId();
        this.deliveryId = route.getDeliveryId();
        this.sequence = route.getSequence();
        this.startHubId = route.getStartHubId();
        this.endHubId = route.getEndHubId();
        this.estimatedDistance = route.getEstimatedDistance();
        this.estimatedDuration = route.getEstimatedDuration();
        this.realDistance = route.getRealDistance();
        this.realDuration = route.getRealDuration();
        this.status = route.getStatus();
        this.deliveryManagerId = route.getDeliveryManagerId();
        this.createdAt = route.getCreatedAt();
    }

    public static DeliveryRouteResponse from(DeliveryRoute route) {
        return new DeliveryRouteResponse(route);
    }
}
