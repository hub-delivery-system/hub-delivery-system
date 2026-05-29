package com.hubdelivery.hubtohub.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class HubTransferInvalidHub extends BaseException {
    public HubTransferInvalidHub(String message) {
        super(HubToHubErrorCode.HUBTRANSFER_INVALID_HUB);
    }
}
