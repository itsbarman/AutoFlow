package com.autoflow.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoflow.common.exception.InvalidOperationException;
import com.autoflow.common.exception.ResourceNotFoundException;
import com.autoflow.customer.dto.CreateCustomerRequest;
import com.autoflow.customer.dto.CustomerResponse;
import com.autoflow.vehicle.VehicleRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link CustomerService}. The repository is mocked so no database
 * is involved. The real mapper is used as a spy since it is pure logic.
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Spy
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_savesAndReturnsResponse() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Ola", "Nordmann", "12345678", "ola@example.com",
                "Storgata 1", "0155", "Oslo");

        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer toSave = invocation.getArgument(0);
            toSave.setId(1L);
            return toSave;
        });

        CustomerResponse response = customerService.createCustomer(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.firstName()).isEqualTo("Ola");
        assertThat(response.email()).isEqualTo("ola@example.com");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void getCustomerById_whenMissing_throwsResourceNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteCustomer_whenMissing_throwsAndDoesNotDelete() {
        when(customerRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.deleteCustomer(5L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(customerRepository, never()).delete(any(Customer.class));
    }

    @Test
    void deleteCustomer_whenOwnsVehicles_throwsInvalidOperation() {
        Customer customer = new Customer();
        customer.setId(3L);
        when(customerRepository.findById(3L)).thenReturn(Optional.of(customer));
        when(vehicleRepository.existsByCustomerId(3L)).thenReturn(true);

        assertThatThrownBy(() -> customerService.deleteCustomer(3L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("vehicles");

        verify(customerRepository, never()).delete(any(Customer.class));
    }
}
