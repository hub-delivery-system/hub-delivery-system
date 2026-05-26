package com.hubdelivery.auth.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class RejectedUserException extends BaseException {

    public RejectedUserException() {
        super(AuthErrorCode.REJECTED_USER);
    }

}
