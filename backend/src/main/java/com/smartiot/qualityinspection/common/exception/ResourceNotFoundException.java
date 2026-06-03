package com.smartiot.qualityinspection.common.exception;

/**
 * Thrown when a requested entity (product, alert, etc.) does not exist.
 * Mapped to HTTP 404 by {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
