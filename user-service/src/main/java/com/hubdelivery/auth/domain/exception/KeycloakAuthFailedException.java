package com.hubdelivery.auth.domain.exception;

import com.hubdelivery.common.exception.BaseException;

public class KeycloakAuthFailedException extends BaseException {

    public KeycloakAuthFailedException() {
        super(AuthErrorCode.KEYCLOAK_AUTH_FAILED);
    }

}
