package com.hubdelivery.company.product.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class ProductHubNotFoundException extends BaseException {
    public ProductHubNotFoundException() {
        super(ProductErrorCode.RELATED_HUB_NOT_FOUND);
    }
}
