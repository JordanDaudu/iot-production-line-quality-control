package com.smartiot.qualityinspection.common.exception;

import java.time.Instant;

/**
 * Consistent error body returned by the REST API. Kept intentionally small so it never
 * exposes stack traces or internal details to clients (see NFR-13 / TC-E2E-19).
 *
 * @param timestamp when the error occurred
 * @param status    HTTP status code
 * @param error     short error label
 * @param message   human-readable description, safe to display
 * @param path      request path that produced the error
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
