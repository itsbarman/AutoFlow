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

        @NotBlank(message = "Registration number must not be blank")
        @Size(max = 15, message = "Registration number must be at most 15 characters")
        String registrationNumber,

        @Size(max = 17, message = "VIN must be at most 17 characters")
        String vin,

        @NotBlank(message = "Make must not be blank")
        @Size(max = 60, message = "Make must be at most 60 characters")
        String make,

        @NotBlank(message = "Model must not be blank")
        @Size(max = 60, message = "Model must be at most 60 characters")
        String model,

        @Min(value = 1900, message = "Model year must be 1900 or later")
        Integer modelYear,

        @PositiveOrZero(message = "Mileage cannot be negative")
        Integer mileage,

        @NotNull(message = "Fuel type is required")
        FuelType fuelType
) {
}
