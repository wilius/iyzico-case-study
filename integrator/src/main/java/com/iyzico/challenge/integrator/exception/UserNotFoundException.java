package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public class UserNotFoundException extends BaseIntegratorException {
    public UserNotFoundException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.USER_NOT_FOUND;
    }
}
