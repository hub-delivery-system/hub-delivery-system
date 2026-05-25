package com.hubdelivery.auth.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class PendingApprovalException extends BaseException {

    public PendingApprovalException() {
        super(AuthErrorCode.PENDING_APPROVAL);
    }

}
