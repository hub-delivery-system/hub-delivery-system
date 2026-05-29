package com.hubdelivery.slack.domain.exception;

import com.hubdelivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SlackErrorCode implements ErrorCode {

    SLACK_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND,        "SLACK-001", "슬랙 메시지를 찾을 수 없습니다."),
    SLACK_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,  "SLACK-002", "슬랙 메시지 전송에 실패했습니다."),
    AI_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND,           "AI-001",    "AI 메시지를 찾을 수 없습니다."),
    AI_API_CALL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI-002",    "AI API 호출에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}