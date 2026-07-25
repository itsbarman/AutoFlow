package com.autoflow.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for a full update (PUT) of an existing customer.
 */
public record UpdateCustomerRequest(

        @NotBlank(message = "First name must not be blank")
        @Size(max = 100, message = "First name must be at most 100 characters")
        String firstName,

        @NotBlank(message = "Last name must not be blank")
        @Size(max = 100, message = "Last name must be at most 100 characters")
        String lastName,

        @NotBlank(message = "Phone number must not be blank")
        @Size(max = 30, message = "Phone number must be at most 30 characters")
        String phoneNumber,

        @Email(message = "Email must be a valid email address")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,

        @Size(max = 255, message = "Address must be at most 255 characters")
        String address,

        @Size(max = 20, message = "Postal code must be at most 20 characters")
        String postalCode,

        @Size(max = 100, message = "City must be at most 100 characters")
        String city
) {
}
