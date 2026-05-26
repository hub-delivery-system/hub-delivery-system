package com.hubdelivery.auth.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class UsernameAlreadyExistsException extends BaseException {

    public UsernameAlreadyExistsException() {
        super(AuthErrorCode.USERNAME_ALREADY_EXISTS);
    }
}
