package com.iyzico.challenge.integrator.exception;

import com.iyzico.challenge.integrator.dto.ErrorCode;

public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    @Override
    public ErrorCode getErrorCode() {
        return ErrorCode.PRODUCT_NOT_FOUND;
    }
}
