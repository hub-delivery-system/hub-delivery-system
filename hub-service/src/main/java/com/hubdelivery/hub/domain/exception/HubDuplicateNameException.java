package com.hubdelivery.hub.domain.exception;
import com.hubdelivery.common.exception.BaseException;


import com.hubdelivery.common.exception.BaseException;

public class HubDuplicateNameException extends BaseException{
    public HubDuplicateNameException() {
        super(HubErrorCode.HUB_DUPLICATE_NAME);
    }
}
