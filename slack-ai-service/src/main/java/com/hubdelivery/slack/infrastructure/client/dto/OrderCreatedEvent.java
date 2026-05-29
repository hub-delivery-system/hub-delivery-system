package com.hubdelivery.slack.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private UUID orderId;
    private UUID producerId;
    private UUID receiverId;
    private UUID productId;
    private Integer amount;
    private String requestMessage;
    private LocalDateTime createdAt;
}
