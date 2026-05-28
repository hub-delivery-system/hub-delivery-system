package com.hubdelivery.company.company.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class CompanyHubNotFoundException extends BaseException {
    public CompanyHubNotFoundException() {
        super(CompanyErrorCode.RELATED_HUB_NOT_FOUND);
    }
}
