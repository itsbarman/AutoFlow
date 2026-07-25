package com.autoflow.customer;

import com.autoflow.common.exception.ResourceNotFoundException;
import com.autoflow.customer.dto.CreateCustomerRequest;
import com.autoflow.customer.dto.CustomerResponse;
import com.autoflow.customer.dto.UpdateCustomerRequest;
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

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
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
        // NOTE: once vehicles and work orders exist, this method must reject deletion
        // when the customer still owns any of them (InvalidOperationException).
        customerRepository.delete(customer);
        log.info("Customer {} deleted", id);
    }

    private Customer findCustomerOrThrow(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", id));
    }
}
