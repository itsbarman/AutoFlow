package com.autoflow.customer;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link Customer}. Used only by the service layer, never by controllers.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
