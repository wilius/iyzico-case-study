package com.iyzico.challenge.integrator.service.hazelcast.exception;

public class CannotHoldTheLockException extends RuntimeException {
    public CannotHoldTheLockException(String message) {
        super(message);
    }

    public CannotHoldTheLockException(Throwable cause) {
        super(cause);
    }
}
