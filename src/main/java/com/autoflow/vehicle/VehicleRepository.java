package com.autoflow.vehicle;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link Vehicle}. Used only by the service layer.
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByCustomerIdOrderByIdAsc(Long customerId);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, Long id);

    boolean existsByVin(String vin);

    boolean existsByVinAndIdNot(String vin, Long id);

    /** Used by the customer delete guard to block deleting an owner with vehicles. */
    boolean existsByCustomerId(Long customerId);
}
