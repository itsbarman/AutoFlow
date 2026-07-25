package com.autoflow.common.exception;

/**
 * Thrown when a requested resource does not exist. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Convenience factory, e.g. {@code ResourceNotFoundException.of("Customer", 10)}.
     */
    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException(resource + " with id " + id + " was not found");
    }
}
