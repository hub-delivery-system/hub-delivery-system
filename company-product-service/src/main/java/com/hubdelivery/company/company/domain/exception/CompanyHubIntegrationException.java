package com.hubdelivery.company.company.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class CompanyHubIntegrationException extends BaseException {
    public CompanyHubIntegrationException() {
        super(CompanyErrorCode.RELATED_HUB_INTEGRATION_FAILED);
    }
}
