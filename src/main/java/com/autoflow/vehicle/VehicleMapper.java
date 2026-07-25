package com.autoflow.vehicle;

import com.autoflow.customer.Customer;
import com.autoflow.vehicle.dto.CreateVehicleRequest;
import com.autoflow.vehicle.dto.UpdateVehicleRequest;
import com.autoflow.vehicle.dto.VehicleResponse;
import org.springframework.stereotype.Component;

/**
 * Converts between {@link Vehicle} entities and the vehicle DTOs.
 */
@Component
public class VehicleMapper {

    /**
     * Builds a new vehicle from a create request and its owning customer.
     * The registration number is expected to be already normalized.
     */
    public Vehicle toEntity(CreateVehicleRequest request, Customer owner, String normalizedRegNumber) {
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomer(owner);
        vehicle.setRegistrationNumber(normalizedRegNumber);
        vehicle.setVin(request.vin());
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setModelYear(request.modelYear());
        vehicle.setMileage(request.mileage());
        vehicle.setFuelType(request.fuelType());
        return vehicle;
    }

    /**
     * Copies values from an update request onto an existing managed entity.
     */
    public void updateEntity(Vehicle vehicle, UpdateVehicleRequest request, String normalizedRegNumber) {
        vehicle.setRegistrationNumber(normalizedRegNumber);
        vehicle.setVin(request.vin());
        vehicle.setMake(request.make());
        vehicle.setModel(request.model());
        vehicle.setModelYear(request.modelYear());
        vehicle.setMileage(request.mileage());
        vehicle.setFuelType(request.fuelType());
    }

    public VehicleResponse toResponse(Vehicle vehicle) {
        Customer owner = vehicle.getCustomer();
        return new VehicleResponse(
                vehicle.getId(),
                owner.getId(),
                owner.getFirstName() + " " + owner.getLastName(),
                vehicle.getRegistrationNumber(),
                vehicle.getVin(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getModelYear(),
                vehicle.getMileage(),
                vehicle.getFuelType(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
