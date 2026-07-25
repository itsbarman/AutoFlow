package com.autoflow.common.exception;

/**
 * Thrown when a unique constraint would be violated (e.g. duplicate registration
 * number or VIN). Maps to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
