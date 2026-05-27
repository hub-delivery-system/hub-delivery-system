package com.hubdelivery.company.company.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class CompanyNotFoundException extends BaseException {
    public CompanyNotFoundException() {
        super(CompanyErrorCode.COMPANY_NOT_FOUND);
    }
}
