package com.hubdelivery.deliveryservice.deliveryroute.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class DeliveryRouteException extends BaseException {

    public DeliveryRouteException(DeliveryRouteErrorCode errorCode) {
        super(errorCode);
    }
}
