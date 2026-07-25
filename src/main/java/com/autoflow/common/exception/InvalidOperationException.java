package com.autoflow.common.exception;

/**
 * Thrown when an operation is not allowed given the current state of the data,
 * e.g. deleting a customer that still owns vehicles. Maps to HTTP 409 Conflict.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
