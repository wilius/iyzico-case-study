package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public class InvalidInstallmentCountException extends BaseIntegratorException {
    public InvalidInstallmentCountException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.INVALID_INSTALLMENT_COUNT;
    }
}
