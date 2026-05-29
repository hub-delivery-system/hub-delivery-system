package com.hubdelivery.slack.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class AiMessageRequestDto {

    @NotBlank(message = "AI 응답 메시지는 필수입니다.")
    private String responseMessage;

    @NotBlank(message = "상태는 필수입니다.")
    private String status;

    private UUID orderId;
}