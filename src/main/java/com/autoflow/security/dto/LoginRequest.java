package com.autoflow.security.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Brukernavn kan ikke være tomt")
        String username,

        @NotBlank(message = "Passord kan ikke være tomt")
        String password
) {
}
