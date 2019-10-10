package com.iyzico.challenge.integrator.exception;

public abstract class ResourceNotFoundException extends BaseIntegratorException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
