package com.hubdelivery.company.company.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class CompanyAccessDeniedException extends BaseException {
    public CompanyAccessDeniedException() {
        super(CompanyErrorCode.COMPANY_ACCESS_DENIED);
    }
}
