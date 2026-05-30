package com.hubdelivery.user.domain.exception;

import org.springframework.util.StringUtils;

import com.hubdelivery.common.exception.BaseException;

public class DeliveryManagerCapacityExceededException extends BaseException {

    public DeliveryManagerCapacityExceededException() {
        super(UserErrorCode.DELIVERY_MANAGER_CAPACITY_EXCEEDED);
    }

    public DeliveryManagerCapacityExceededException(String message) {
        super(
                UserErrorCode.DELIVERY_MANAGER_CAPACITY_EXCEEDED,
                StringUtils.hasText(message)
                        ? message
                        : UserErrorCode.DELIVERY_MANAGER_CAPACITY_EXCEEDED.getMessage()
        );
    }
}
