package com.autoflow.customer;

import static org.assertj.core.api.Assertions.assertThat;

import com.autoflow.support.PostgresContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository tests running against a real PostgreSQL container.
 * Flyway builds the schema, then Hibernate validates the entity mapping against it.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CustomerRepositoryTest extends PostgresContainerSupport {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void save_populatesIdAndAuditFields() {
        Customer customer = new Customer();
        customer.setFirstName("Kari");
        customer.setLastName("Nordmann");
        customer.setPhoneNumber("98765432");
        customer.setEmail("kari@example.com");

        Customer saved = customerRepository.save(customer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_returnsPersistedCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("Per");
        customer.setLastName("Hansen");
        customer.setPhoneNumber("11223344");
        Long id = customerRepository.save(customer).getId();

        assertThat(customerRepository.findById(id)).isPresent();
    }
}
