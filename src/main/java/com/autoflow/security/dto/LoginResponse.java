package com.autoflow.security.dto;

import java.util.List;

/**
 * Returned after a successful login. The token goes in the Authorization header
 * on subsequent requests.
 */
public record LoginResponse(
        String token,
        long expiresInMs,
        String username,
        String fullName,
        List<String> roles
) {
}
