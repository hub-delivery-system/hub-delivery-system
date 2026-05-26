package com.hubdelivery.auth.domain.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.hubdelivery.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    SLACK_ID_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "AUTH-001", "이미 사용 중인 slack_id 입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "AUTH-003", "슬랙 아이디 또는 비밀번호가 올바르지 않습니다."),
    PENDING_APPROVAL(HttpStatus.FORBIDDEN, "AUTH-004", "가입 승인 대기 중입니다."),
    REJECTED_USER(HttpStatus.FORBIDDEN, "AUTH-005", "가입이 거절된 계정입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
