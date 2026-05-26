package com.hubdelivery.auth.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class SlackIdAlreadyExistsException extends BaseException {

    public SlackIdAlreadyExistsException() {
        super(AuthErrorCode.SLACK_ID_ALREADY_EXISTS);
    }

}
