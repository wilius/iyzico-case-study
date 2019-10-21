package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public class CannotCreateSessionException extends BaseIntegratorException {
    public CannotCreateSessionException(String message) {
        super(message);
    }

    public CannotCreateSessionException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.UNEXPECTED_ERROR;
    }
}
