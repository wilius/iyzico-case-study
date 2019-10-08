package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public abstract class BaseIntegratorException extends RuntimeException {
    public BaseIntegratorException(String message) {
        super(message);
    }

    public BaseIntegratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract ErrorCode getErrorCode();
}
