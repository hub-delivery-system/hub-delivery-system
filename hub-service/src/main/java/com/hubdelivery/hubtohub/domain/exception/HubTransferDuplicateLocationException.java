package com.hubdelivery.hubtohub.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class HubTransferDuplicateLocationException extends BaseException {
    public HubTransferDuplicateLocationException(String message) {
        super(HubToHubErrorCode.HUBTRANSFER_DUPLICATE_LOCATION);
    }
}
