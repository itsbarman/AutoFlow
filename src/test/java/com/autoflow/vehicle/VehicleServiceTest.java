package com.autoflow.vehicle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoflow.common.exception.DuplicateResourceException;
import com.autoflow.common.exception.ResourceNotFoundException;
import com.autoflow.customer.Customer;
import com.autoflow.customer.CustomerRepository;
import com.autoflow.vehicle.dto.CreateVehicleRequest;
import com.autoflow.vehicle.dto.VehicleResponse;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link VehicleService} with mocked repositories.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Spy
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleService vehicleService;

    private Customer owner() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Ola");
        customer.setLastName("Nordmann");
        return customer;
    }

    private CreateVehicleRequest request(String regNumber) {
        return new CreateVehicleRequest(regNumber, null, "Toyota", "Corolla", 2020, 45000, FuelType.PETROL);
    }

    @Test
    void createVehicle_normalizesRegistrationNumberAndSaves() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(vehicleRepository.existsByRegistrationNumber("AB12345")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> {
            Vehicle v = inv.getArgument(0);
            v.setId(10L);
            return v;
        });

        // Lower case with spaces should be normalized to "AB12345".
        VehicleResponse response = vehicleService.createVehicle(1L, request("ab 123 45"));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.registrationNumber()).isEqualTo("AB12345");
        assertThat(response.customerId()).isEqualTo(1L);
        assertThat(response.customerName()).isEqualTo("Ola Nordmann");
    }

    @Test
    void createVehicle_whenCustomerMissing_throwsNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.createVehicle(99L, request("AB12345")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_whenRegistrationNumberExists_throwsDuplicate() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(owner()));
        when(vehicleRepository.existsByRegistrationNumber("AB12345")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.createVehicle(1L, request("AB12345")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("AB12345");

        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }
}
