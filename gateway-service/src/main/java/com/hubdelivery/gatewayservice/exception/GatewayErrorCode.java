package com.hubdelivery.gatewayservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GatewayErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "GATEWAY-001", "요청한 경로를 찾을 수 없습니다."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "GATEWAY-002", "서비스에 일시적으로 접근할 수 없습니다."),

    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "GATEWAY-101", "만료된 토큰입니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "GATEWAY-102", "유효하지 않은 토큰입니다."),
    TOKEN_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "GATEWAY-103", "지원하지 않는 토큰입니다."),
    TOKEN_EMPTY(HttpStatus.UNAUTHORIZED, "GATEWAY-104", "토큰 값이 비어 있습니다."),

    GATEWAY_ERROR(HttpStatus.BAD_GATEWAY, "GATEWAY-901", "게이트웨이 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GATEWAY-999", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
