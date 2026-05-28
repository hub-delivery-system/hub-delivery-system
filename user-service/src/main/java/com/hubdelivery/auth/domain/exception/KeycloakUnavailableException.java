package com.hubdelivery.auth.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class KeycloakUnavailableException extends BaseException {

    public KeycloakUnavailableException() {
        super(AuthErrorCode.KEYCLOAK_UNAVAILABLE);
    }

}
