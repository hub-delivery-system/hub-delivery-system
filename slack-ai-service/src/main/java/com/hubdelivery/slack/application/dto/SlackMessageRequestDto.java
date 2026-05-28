package com.hubdelivery.slack.application.dto;

import com.hubdelivery.slack.domain.entity.SlackMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SlackMessageRequestDto {

    @NotNull(message = "발신자 ID는 필수입니다.")
    private UUID userId;

    @NotBlank(message = "수신자 슬랙 ID는 필수입니다.")
    private String slackId;

    @NotBlank(message = "메시지는 필수입니다.")
    private String message;

    // AI 알림 등 내부 시스템 발송용 — userId null 허용, validation 우회
    public static SlackMessageRequestDto ofSystem(String slackId, String message) {
        return new SlackMessageRequestDto(null, slackId, message);
    }

    public SlackMessage toEntity() {
        return SlackMessage.builder()
                .userId(userId)
                .slackId(slackId)
                .message(message)
                .build();
    }
}