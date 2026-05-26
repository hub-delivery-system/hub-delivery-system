package com.hubdelivery.hub.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class HubAddressInvalidException extends BaseException {
    public HubAddressInvalidException() {
        super(HubErrorCode.HUB_ADDRESS_INVALID);
    }
}
