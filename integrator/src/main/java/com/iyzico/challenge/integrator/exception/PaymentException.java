package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public class PaymentException extends BaseIntegratorException {
    public PaymentException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.PAYMENT_EXCEPTION;
    }
}
