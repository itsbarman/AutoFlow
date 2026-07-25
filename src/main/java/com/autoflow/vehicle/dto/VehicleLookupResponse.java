package com.autoflow.vehicle.dto;

import com.autoflow.vehicle.FuelType;

/**
 * Simplified view of vehicle data returned from the Statens Vegvesen lookup.
 * Only fields we actually store are included; unknown data is discarded.
 */
public record VehicleLookupResponse(
        String registrationNumber,
        String make,
        String model,
        Integer modelYear,
        FuelType fuelType,
        String vin,
        String color
) {
}
