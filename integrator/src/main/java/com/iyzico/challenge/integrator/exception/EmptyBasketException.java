package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public class EmptyBasketException extends BaseIntegratorException {
    public EmptyBasketException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.EMPTY_BASKET;
    }
}
