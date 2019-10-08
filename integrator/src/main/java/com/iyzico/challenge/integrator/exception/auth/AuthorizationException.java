package com.iyzico.challenge.integrator.exception.auth;

import com.iyzico.challenge.integrator.dto.ErrorCode;
import com.iyzico.challenge.integrator.exception.BaseIntegratorException;

public class AuthorizationException extends BaseIntegratorException {
    public AuthorizationException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.NOT_AUTHORIZED;
    }
}
