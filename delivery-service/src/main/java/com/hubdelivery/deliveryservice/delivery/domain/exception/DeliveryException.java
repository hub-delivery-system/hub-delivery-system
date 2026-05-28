package com.hubdelivery.deliveryservice.delivery.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class DeliveryException extends BaseException {

    public DeliveryException(DeliveryErrorCode errorCode) {
        super(errorCode);
    }
}
