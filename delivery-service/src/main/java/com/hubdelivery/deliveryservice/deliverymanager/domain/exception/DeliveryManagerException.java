package com.hubdelivery.deliveryservice.deliverymanager.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class DeliveryManagerException extends BaseException {

    public DeliveryManagerException(DeliveryManagerErrorCode errorCode) {
        super(errorCode);
    }
}
