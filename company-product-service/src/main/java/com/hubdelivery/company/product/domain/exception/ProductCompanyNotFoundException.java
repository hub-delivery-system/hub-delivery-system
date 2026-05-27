package com.hubdelivery.company.product.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class ProductCompanyNotFoundException extends BaseException {
    public ProductCompanyNotFoundException() {
        super(ProductErrorCode.PRODUCT_COMPANY_NOT_FOUND);
    }
}
