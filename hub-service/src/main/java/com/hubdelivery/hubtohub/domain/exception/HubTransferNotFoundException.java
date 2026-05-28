package com.hubdelivery.hubtohub.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class HubTransferNotFoundException extends BaseException {
    public HubTransferNotFoundException() {
        super(HubToHubErrorCode.HUBTRANSFER_NOT_FOUND);
    }
}
