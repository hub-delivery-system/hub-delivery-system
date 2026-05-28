package com.hubdelivery.orderservice.order.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class OrderException extends BaseException {

    public OrderException(OrderErrorCode errorCode) {
        super(errorCode);
    }
}
