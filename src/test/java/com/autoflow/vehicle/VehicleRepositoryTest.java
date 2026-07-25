package com.autoflow.vehicle;

import static org.assertj.core.api.Assertions.assertThat;

import com.autoflow.customer.Customer;
import com.autoflow.customer.CustomerRepository;
import com.autoflow.support.PostgresContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository tests for vehicles against a real PostgreSQL container.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class VehicleRepositoryTest extends PostgresContainerSupport {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer persistedCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Ola");
        customer.setLastName("Nordmann");
        customer.setPhoneNumber("12345678");
        return customerRepository.save(customer);
    }

    private Vehicle vehicle(Customer owner, String regNumber) {
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomer(owner);
        vehicle.setRegistrationNumber(regNumber);
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setFuelType(FuelType.PETROL);
        return vehicle;
    }

    @Test
    void save_populatesIdAndAuditFields() {
        Vehicle saved = vehicleRepository.save(vehicle(persistedCustomer(), "AB12345"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void existsByRegistrationNumber_and_existsByCustomerId_work() {
        Customer owner = persistedCustomer();
        vehicleRepository.save(vehicle(owner, "CD54321"));

        assertThat(vehicleRepository.existsByRegistrationNumber("CD54321")).isTrue();
        assertThat(vehicleRepository.existsByRegistrationNumber("ZZ00000")).isFalse();
        assertThat(vehicleRepository.existsByCustomerId(owner.getId())).isTrue();
        assertThat(vehicleRepository.findByCustomerIdOrderByIdAsc(owner.getId())).hasSize(1);
    }
}
