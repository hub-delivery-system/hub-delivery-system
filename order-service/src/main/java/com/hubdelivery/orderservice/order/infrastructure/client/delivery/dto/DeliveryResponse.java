package com.hubdelivery.orderservice.order.infrastructure.client.delivery.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeliveryResponse {

    // 주문에 deliveryId를 연결하기 위해 id만 사용
    private UUID id;
    private UUID orderId;
    private String status;
}
