package com.hubdelivery.company.product.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class ProductNotFoundException extends BaseException {
    public ProductNotFoundException() {
        super(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
}
