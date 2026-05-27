package com.hubdelivery.hub.domain.exception;


import com.hubdelivery.common.exception.BaseException;

public class HubNotFoundException extends BaseException{
    public HubNotFoundException() {
        super(HubErrorCode.HUB_NOT_FOUND);
    }
}
