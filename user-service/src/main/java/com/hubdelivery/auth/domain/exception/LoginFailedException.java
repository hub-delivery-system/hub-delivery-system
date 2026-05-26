package com.hubdelivery.auth.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class LoginFailedException extends BaseException {

    public LoginFailedException() {
        super(AuthErrorCode.LOGIN_FAILED);
    }

}
