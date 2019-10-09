package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public class UsernameTakenByAnotherUserException extends BaseIntegratorException {
    public UsernameTakenByAnotherUserException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.USERNAME_TAKEN_BY_ANOTHER_USER;
    }
}
