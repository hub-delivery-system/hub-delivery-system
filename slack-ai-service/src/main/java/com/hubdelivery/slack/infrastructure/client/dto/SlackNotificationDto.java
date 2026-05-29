package com.hubdelivery.slack.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackNotificationDto {
    private UUID orderId;
    private String shippingStatus;
    private String route;
    private String hubName;
    private String hubManagerName;
    private String hubManagerSlackId;
    private String message;
}