package com.autoflow.common.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Consistent error response structure used by {@link GlobalExceptionHandler}.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ValidationError> validationErrors
) {

    public static ApiError of(int status, String error, String message, String path,
                              List<ValidationError> validationErrors) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}
