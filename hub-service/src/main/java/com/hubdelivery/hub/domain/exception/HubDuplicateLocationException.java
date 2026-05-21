package com.hubdelivery.hub.domain.exception;

import com.hubdelivery.common.exception.BaseException;


public class HubDuplicateLocationException extends BaseException{
    public HubDuplicateLocationException() {
        super(HubErrorCode.HUB_DUPLICATE_LOCATION);
    }
}
