package com.autoflow.common.exception;

/**
 * A single field validation error, returned inside {@link ApiError#validationErrors()}.
 */
public record ValidationError(String field, String message) {
}
