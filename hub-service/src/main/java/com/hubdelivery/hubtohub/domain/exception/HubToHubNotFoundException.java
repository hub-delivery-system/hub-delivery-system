package com.hubdelivery.hubtohub.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class HubToHubNotFoundException extends BaseException {
    public HubToHubNotFoundException() {
        super(HubToHubErrorCode.HUBTOHUB_NOT_FOUND);
    }
}
