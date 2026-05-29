package com.hubdelivery.company.product.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class ProductStockNotEnoughException extends BaseException {
    public ProductStockNotEnoughException() {
        super(ProductErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
    }
}
