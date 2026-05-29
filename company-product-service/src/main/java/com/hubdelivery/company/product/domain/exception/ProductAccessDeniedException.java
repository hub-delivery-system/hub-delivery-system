package com.hubdelivery.company.product.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class ProductAccessDeniedException extends BaseException {
    public ProductAccessDeniedException() {
        super(ProductErrorCode.PRODUCT_ACCESS_DENIED);
    }
}
