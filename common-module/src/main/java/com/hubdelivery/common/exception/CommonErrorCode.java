package com.hubdelivery.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    /** 입력 및 검증 관련 에러 코드 */
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "COMMON-001", "요청 값이 올바르지 않습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-002", "잘못된 입력값입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-003", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-004", "허용되지 않는 메서드입니다."),

    /** 인증 및 인가 관련 에러 코드 */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-101", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-102", "접근 권한이 없습니다."),

    /** 외부 연동 및 서버 관련 에러 코드 */
    EXTERNAL_API_FAILURE(HttpStatus.BAD_GATEWAY, "COMMON-901", "외부 API 호출에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-999", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
