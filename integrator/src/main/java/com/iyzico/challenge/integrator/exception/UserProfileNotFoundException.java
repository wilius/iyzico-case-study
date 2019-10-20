package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public class UserProfileNotFoundException extends ResourceNotFoundException {
    public UserProfileNotFoundException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.USER_PROFILE_NOT_FOUND;
    }
}
