package com.hubdelivery.hub.domain.exception;
import com.hubdelivery.common.exception.BaseException;

public class HubInvalidCoordinatesException extends BaseException{
    public HubInvalidCoordinatesException() {
        super(HubErrorCode.HUB_ADDRESS_INVALID);
    }
}
