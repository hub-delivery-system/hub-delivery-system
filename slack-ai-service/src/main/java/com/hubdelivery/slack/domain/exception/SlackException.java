package com.hubdelivery.slack.domain.exception;

import com.hubdelivery.common.exception.BaseException;

// CommonException은 CommonErrorCode만 받으므로 BaseException 직접 상속
// common-module의 GlobalExceptionHandler가 BaseException 잡아주므로 별도 핸들러 불필요
public class SlackException extends BaseException {

    public SlackException(SlackErrorCode errorCode) {
        super(errorCode);
    }

    public SlackException(SlackErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}