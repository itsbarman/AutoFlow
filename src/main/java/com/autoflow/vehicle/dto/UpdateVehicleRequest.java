package com.autoflow.vehicle.dto;

import com.autoflow.vehicle.FuelType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Request body for a full update (PUT) of an existing vehicle.
 */
public record UpdateVehicleRequest(

        @NotBlank(message = "Registreringsnummer kan ikke være tomt")
        @Size(max = 15, message = "Registreringsnummer kan være maks 15 tegn")
        String registrationNumber,

        @Size(max = 17, message = "VIN kan være maks 17 tegn")
        String vin,

        @NotBlank(message = "Merke kan ikke være tomt")
        @Size(max = 60, message = "Merke kan være maks 60 tegn")
        String make,

        @NotBlank(message = "Modell kan ikke være tomt")
        @Size(max = 60, message = "Modell kan være maks 60 tegn")
        String model,

        @Min(value = 1900, message = "Årsmodell må være 1900 eller senere")
        Integer modelYear,

        @PositiveOrZero(message = "Kilometerstand kan ikke være negativ")
        Integer mileage,

        @NotNull(message = "Drivstoff er påkrevd")
        FuelType fuelType
) {
}
