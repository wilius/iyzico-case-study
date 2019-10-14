package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public class StockNotEnoughException extends BaseIntegratorException {
    public StockNotEnoughException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.STOCK_NOT_ENOUGH;
    }
}
