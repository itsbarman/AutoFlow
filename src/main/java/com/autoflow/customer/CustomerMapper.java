package com.autoflow.customer;

import com.autoflow.customer.dto.CreateCustomerRequest;
import com.autoflow.customer.dto.CustomerResponse;
import com.autoflow.customer.dto.UpdateCustomerRequest;
import org.springframework.stereotype.Component;

/**
 * Converts between {@link Customer} entities and the customer DTOs.
 * Keeping this in one place stops mapping logic from leaking into the service.
 */
@Component
public class CustomerMapper {

    /**
     * Builds a new entity from a create request. Audit fields are set automatically.
     */
    public Customer toEntity(CreateCustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        customer.setPostalCode(request.postalCode());
        customer.setCity(request.city());
        return customer;
    }

    /**
     * Copies values from an update request onto an existing managed entity.
     */
    public void updateEntity(Customer customer, UpdateCustomerRequest request) {
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPhoneNumber(request.phoneNumber());
        customer.setEmail(request.email());
        customer.setAddress(request.address());
        customer.setPostalCode(request.postalCode());
        customer.setCity(request.city());
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getPostalCode(),
                customer.getCity(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
