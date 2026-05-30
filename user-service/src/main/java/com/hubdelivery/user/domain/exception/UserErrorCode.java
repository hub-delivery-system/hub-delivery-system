package com.hubdelivery.user.domain.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.hubdelivery.common.exception.ErrorCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    DELIVERY_MANAGER_CAPACITY_EXCEEDED(HttpStatus.CONFLICT, "USER-001", "배송 담당자 정원이 초과되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
