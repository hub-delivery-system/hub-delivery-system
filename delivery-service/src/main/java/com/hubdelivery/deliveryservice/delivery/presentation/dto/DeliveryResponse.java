package com.hubdelivery.deliveryservice.delivery.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hubdelivery.deliveryservice.delivery.domain.entity.Delivery;
import com.hubdelivery.deliveryservice.delivery.domain.type.DeliveryStatus;
import com.hubdelivery.deliveryservice.deliveryroute.presentation.dto.DeliveryRouteResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class DeliveryResponse {

    private final UUID id;
    private final UUID orderId;
    private final DeliveryStatus status;
    private final UUID startHubId;
    private final UUID endHubId;
    private final String address;
    private final UUID userId;
    private final String slackId;
    private final UUID deliveryManagerId;
    private final LocalDateTime createdAt;

    // getById/create 시에만 포함, getAll 시 null로 직렬화 제외
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<DeliveryRouteResponse> routes;

    private DeliveryResponse(Delivery delivery, List<DeliveryRouteResponse> routes) {
        this.id = delivery.getId();
        this.orderId = delivery.getOrderId();
        this.status = delivery.getStatus();
        this.startHubId = delivery.getStartHubId();
        this.endHubId = delivery.getEndHubId();
        this.address = delivery.getAddress();
        this.userId = delivery.getUserId();
        this.slackId = delivery.getSlackId();
        this.deliveryManagerId = delivery.getDeliveryManagerId();
        this.createdAt = delivery.getCreatedAt();
        this.routes = routes;
    }

    public static DeliveryResponse from(Delivery delivery) {
        return new DeliveryResponse(delivery, null);
    }

    public static DeliveryResponse withRoutes(Delivery delivery, List<DeliveryRouteResponse> routes) {
        return new DeliveryResponse(delivery, routes);
    }
}
