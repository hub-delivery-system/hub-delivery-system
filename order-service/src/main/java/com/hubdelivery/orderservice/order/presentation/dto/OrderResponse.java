package com.hubdelivery.orderservice.order.presentation.dto;

import com.hubdelivery.orderservice.order.domain.entity.Order;
import com.hubdelivery.orderservice.order.domain.type.OrderStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OrderResponse {

    private final UUID id;
    private final UUID producerId;
    private final UUID receiverId;
    private final UUID productId;
    private final Integer amount;
    private final UUID deliveryId;
    private final String requestMessage;
    private final OrderStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private OrderResponse(Order order) {
        this.id = order.getId();
        this.producerId = order.getProducerId();
        this.receiverId = order.getReceiverId();
        this.productId = order.getProductId();
        this.amount = order.getAmount();
        this.deliveryId = order.getDeliveryId();
        this.requestMessage = order.getRequestMessage();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(order);
    }
}
