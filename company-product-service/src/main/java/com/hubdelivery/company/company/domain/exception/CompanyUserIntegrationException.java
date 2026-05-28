package com.hubdelivery.company.company.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class CompanyUserIntegrationException extends BaseException {
    public CompanyUserIntegrationException() {
        super(CompanyErrorCode.RELATED_USER_INTEGRATION_FAILED);
    }
}
