package com.hubdelivery.hubtohub.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class HubTransferInvalidCentralHub extends BaseException {

    public HubTransferInvalidCentralHub(String message) {
        super(HubToHubErrorCode.HUBTRANSFER_INVALID_CENTRALHUB);
    }
}
