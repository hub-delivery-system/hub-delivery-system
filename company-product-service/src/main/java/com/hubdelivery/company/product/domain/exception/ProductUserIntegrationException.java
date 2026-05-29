package com.hubdelivery.company.product.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class ProductUserIntegrationException extends BaseException {
    public ProductUserIntegrationException() {
        super(ProductErrorCode.RELATED_USER_INTEGRATION_FAILED);
    }
}
