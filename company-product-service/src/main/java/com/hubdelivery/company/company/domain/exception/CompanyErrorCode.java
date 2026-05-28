package com.hubdelivery.company.company.domain.exception;

import com.hubdelivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {

    INVALID_COMPANY_TYPE(HttpStatus.BAD_REQUEST, "COMPANY-001", "유효하지 않은 업체 타입입니다."),
    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY-002", "업체를 찾을 수 없습니다."),
    RELATED_HUB_NOT_FOUND(HttpStatus.NOT_FOUND, "COMPANY-003", "관리 허브를 찾을 수 없습니다."),

    RELATED_HUB_INTEGRATION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "COMPANY-901", "허브 서비스 연동에 실패했습니다."),
    RELATED_USER_INTEGRATION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "COMPANY-902", "사용자 서비스 연동에 실패했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
