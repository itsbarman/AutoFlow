package com.autoflow.customer;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoflow.customer.dto.CreateCustomerRequest;
import com.autoflow.support.PostgresContainerSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full-stack API tests: HTTP -> controller -> service -> repository -> PostgreSQL.
 * Also covers validation and error-handling behaviour.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerIntegrationTest extends PostgresContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndFetchCustomer_returnsCreatedCustomer() throws Exception {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Ola", "Nordmann", "12345678", "ola@example.com",
                "Storgata 1", "0155", "Oslo");

        String location = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").value("Ola"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Nordmann"));
    }

    @Test
    void getCustomer_whenMissing_returns404WithApiError() throws Exception {
        mockMvc.perform(get("/api/v1/customers/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/customers/999999"));
    }

    @Test
    void createCustomer_withInvalidData_returns400WithValidationErrors() throws Exception {
        // Blank first name + invalid email should both fail validation.
        CreateCustomerRequest request = new CreateCustomerRequest(
                "", "Nordmann", "12345678", "not-an-email",
                null, null, null);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors", hasSize(2)));
    }
}
