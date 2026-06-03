package com.smartiot.qualityinspection.common.exception;

/**
 * Thrown when a sensor packet or request fails domain validation rules.
 * Mapped to HTTP 400 by {@link GlobalExceptionHandler}.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
