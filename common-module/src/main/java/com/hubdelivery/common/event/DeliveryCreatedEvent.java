package com.hubdelivery.common.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryCreatedEvent {

    private UUID orderId;
    private UUID deliveryId;
}
