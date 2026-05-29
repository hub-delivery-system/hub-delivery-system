package com.hubdelivery.slack.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class SlackSearchRequestDto {
    private String message;
    private UUID userId;
    private String slackId;
}