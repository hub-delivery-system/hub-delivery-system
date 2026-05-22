package com.hubdelivery.company.company.domain.exception;

import com.hubdelivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {

    INVALID_COMPANY_TYPE(HttpStatus.BAD_REQUEST, "COMPANY-001", "유효하지 않은 업체 타입입니다."),
    ;
    private final HttpStatus status;
    private final String code;
    private final String message;
}
