package com.autoflow.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a new customer.
 */
public record CreateCustomerRequest(

        @NotBlank(message = "Fornavn kan ikke være tomt")
        @Size(max = 100, message = "Fornavn kan være maks 100 tegn")
        String firstName,

        @NotBlank(message = "Etternavn kan ikke være tomt")
        @Size(max = 100, message = "Etternavn kan være maks 100 tegn")
        String lastName,

        @NotBlank(message = "Telefonnummer kan ikke være tomt")
        @Size(max = 30, message = "Telefonnummer kan være maks 30 tegn")
        String phoneNumber,

        @Email(message = "E-post må være en gyldig e-postadresse")
        @Size(max = 255, message = "E-post kan være maks 255 tegn")
        String email,

        @Size(max = 255, message = "Adresse kan være maks 255 tegn")
        String address,

        @Size(max = 20, message = "Postnummer kan være maks 20 tegn")
        String postalCode,

        @Size(max = 100, message = "By kan være maks 100 tegn")
        String city
) {
}
