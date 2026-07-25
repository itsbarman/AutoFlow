package com.autoflow.vehicle.dto;

import com.autoflow.vehicle.FuelType;
import java.time.Instant;

/**
 * Response body for a vehicle. Includes a small summary of the owning customer
 * so the UI can show a "customer" column without a second request.
 */
public record VehicleResponse(
        Long id,
        Long customerId,
        String customerName,
        String registrationNumber,
        String vin,
        String make,
        String model,
        Integer modelYear,
        Integer mileage,
        FuelType fuelType,
        Instant createdAt,
        Instant updatedAt
) {
}
