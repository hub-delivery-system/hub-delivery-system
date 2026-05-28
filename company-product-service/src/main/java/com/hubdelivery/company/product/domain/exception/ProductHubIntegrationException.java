package com.hubdelivery.company.product.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class ProductHubIntegrationException extends BaseException {
    public ProductHubIntegrationException() {
        super(ProductErrorCode.RELATED_HUB_INTEGRATION_FAILED);
    }
}
