package com.iyzico.challenge.payment.properties.exception;

public class CardNumberPropertyValidationException extends RuntimeException {
    public CardNumberPropertyValidationException(String message) {
        super(message);
    }

    public CardNumberPropertyValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
