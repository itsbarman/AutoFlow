package com.autoflow.workorder.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request body for updating a work order line.
 */
public record UpdateWorkOrderLineRequest(

        @NotBlank(message = "Beskrivelse kan ikke være tom")
        @Size(max = 200, message = "Beskrivelse kan være maks 200 tegn")
        String description,

        @Size(max = 60, message = "Delenummer kan være maks 60 tegn")
        String partNumber,

        @NotNull(message = "Antall er påkrevd")
        @DecimalMin(value = "0.01", message = "Antall må være større enn 0")
        BigDecimal quantity,

        @NotNull(message = "Enhetspris er påkrevd")
        @DecimalMin(value = "0.0", message = "Enhetspris kan ikke være negativ")
        BigDecimal unitPrice
) {
}
