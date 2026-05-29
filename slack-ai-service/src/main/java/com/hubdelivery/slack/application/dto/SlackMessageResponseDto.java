package com.hubdelivery.slack.application.dto;

import com.hubdelivery.slack.domain.entity.SlackMessage;
import com.hubdelivery.slack.domain.type.SendStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class SlackMessageResponseDto {

    private final UUID id;
    private final UUID userId;
    private final String slackId;
    private final String message;
    private final LocalDateTime sentAt;
    private final SendStatus status;

    public SlackMessageResponseDto(SlackMessage slack) {
        this.id      = slack.getId();
        this.userId  = slack.getUserId();
        this.slackId = slack.getSlackId();
        this.message = slack.getMessage();
        this.sentAt  = slack.getSentAt();
        this.status  = slack.getStatus();
    }
}