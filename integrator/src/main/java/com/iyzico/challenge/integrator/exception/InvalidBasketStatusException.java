package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public class InvalidBasketStatusException extends BaseIntegratorException {
    public InvalidBasketStatusException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.INVALID_BASKET_STATUS;
    }
}
