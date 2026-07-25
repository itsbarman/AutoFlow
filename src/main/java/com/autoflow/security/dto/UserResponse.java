package com.autoflow.security.dto;

import java.util.List;

/**
 * A safe view of the current user (never includes the password).
 */
public record UserResponse(
        Long id,
        String username,
        String fullName,
        List<String> roles
) {
}
