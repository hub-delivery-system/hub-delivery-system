package com.hubdelivery.common.exception;

public class CommonException extends BaseException {

    public CommonException(CommonErrorCode errorCode) {
        super(errorCode);
    }

    public CommonException(CommonErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
