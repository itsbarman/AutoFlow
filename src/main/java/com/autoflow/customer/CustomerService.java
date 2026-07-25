package com.autoflow.customer;

import com.autoflow.common.exception.InvalidOperationException;
import com.autoflow.common.exception.ResourceNotFoundException;
import com.autoflow.customer.dto.CreateCustomerRequest;
import com.autoflow.customer.dto.CustomerResponse;
import com.autoflow.customer.dto.UpdateCustomerRequest;
import com.autoflow.vehicle.VehicleRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for customers. Controllers talk to this class, never to the
 * repository directly. Dependencies are provided through constructor injection.
 */
@Service
@Transactional
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final VehicleRepository vehicleRepository;

    public CustomerService(CustomerRepository customerRepository,
                           CustomerMapper customerMapper,
                           VehicleRepository vehicleRepository) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.vehicleRepository = vehicleRepository;
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        Customer customer = customerMapper.toEntity(request);
        Customer saved = customerRepository.save(customer);
        log.info("Customer created with id {}", saved.getId());
        return customerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        return customerMapper.toResponse(findCustomerOrThrow(id));
    }

    public CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request) {
        Customer customer = findCustomerOrThrow(id);
        customerMapper.updateEntity(customer, request);
        Customer saved = customerRepository.save(customer);
        log.info("Customer {} updated", saved.getId());
        return customerMapper.toResponse(saved);
    }

    public void deleteCustomer(Long id) {
        Customer customer = findCustomerOrThrow(id);
        // Business data must not disappear implicitly: a customer that still owns
        // vehicles cannot be deleted. (Work orders will be added here later too.)
        if (vehicleRepository.existsByCustomerId(id)) {
            log.info("Rejected deletion of customer {} because they still own vehicles", id);
            throw new InvalidOperationException(
                    "Customer cannot be deleted while they still own vehicles");
        }
        customerRepository.delete(customer);
        log.info("Customer {} deleted", id);
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
    }
}
