package com.autoflow.customer.dto;

import java.time.Instant;

/**
 * Response body returned to API clients. Contains only what the client should see.
 */
public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String phoneNumber,
        String email,
        String address,
        String postalCode,
        String city,
        Instant createdAt,
        Instant updatedAt
) {
}
