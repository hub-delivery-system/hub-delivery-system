package com.hubdelivery.slack.application.dto;

import com.hubdelivery.slack.domain.entity.AiMessage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AiMessageResponseDto {

    private final UUID id;
    private final String responseMessage;
    private final UUID orderId;
    private final String status;
    private final LocalDateTime createdAt;

    public AiMessageResponseDto(AiMessage aiMessage) {
        this.id              = aiMessage.getId();
        this.responseMessage = aiMessage.getResponseMessage();
        this.orderId         = aiMessage.getOrderId();
        this.status          = aiMessage.getStatus();
        this.createdAt       = aiMessage.getCreatedAt();
    }
}