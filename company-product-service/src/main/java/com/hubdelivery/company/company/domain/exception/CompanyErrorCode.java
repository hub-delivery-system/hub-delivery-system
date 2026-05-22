package com.hubdelivery.company.company.domain.exception;

import com.hubdelivery.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CompanyErrorCode implements ErrorCode {

    // Company Error code
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
