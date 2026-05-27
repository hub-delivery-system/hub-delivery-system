package com.hubdelivery.company.company.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class InvalidCompanyTypeException extends BaseException {
    public InvalidCompanyTypeException() {
        super(CompanyErrorCode.INVALID_COMPANY_TYPE);
    }
}
