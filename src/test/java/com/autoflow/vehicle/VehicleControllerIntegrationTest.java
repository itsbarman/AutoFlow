package com.autoflow.vehicle;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.autoflow.support.PostgresContainerSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Full-stack API tests for vehicles, including uniqueness, validation and the
 * "cannot delete a customer that owns vehicles" rule.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class VehicleControllerIntegrationTest extends PostgresContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long createCustomer() throws Exception {
        String body = """
                {"firstName":"Ola","lastName":"Nordmann","phoneNumber":"12345678"}
                """;
        String response = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String vehicleBody(String regNumber) {
        return """
                {"registrationNumber":"%s","make":"Toyota","model":"Corolla",
                 "modelYear":2020,"mileage":45000,"fuelType":"PETROL"}
                """.formatted(regNumber);
    }

    @Test
    void createVehicle_underCustomer_normalizesAndReturnsCreated() throws Exception {
        long customerId = createCustomer();

        mockMvc.perform(post("/api/v1/customers/" + customerId + "/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleBody("ab 123 45")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("AB12345"))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.fuelType").value("PETROL"));
    }

    @Test
    void createVehicle_withDuplicateRegistrationNumber_returns409() throws Exception {
        long customerId = createCustomer();
        mockMvc.perform(post("/api/v1/customers/" + customerId + "/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleBody("DUP1000")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/customers/" + customerId + "/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleBody("DUP1000")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createVehicle_withNegativeMileage_returns400() throws Exception {
        long customerId = createCustomer();
        String body = """
                {"registrationNumber":"NEG1000","make":"Toyota","model":"Corolla",
                 "mileage":-5,"fuelType":"PETROL"}
                """;
        mockMvc.perform(post("/api/v1/customers/" + customerId + "/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("mileage"));
    }

    @Test
    void deleteCustomer_withVehicles_returns409() throws Exception {
        long customerId = createCustomer();
        mockMvc.perform(post("/api/v1/customers/" + customerId + "/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleBody("GUARD10")))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/customers/" + customerId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("kjøretøy")));
    }

    @Test
    void getVehicles_filteredByCustomer_returnsOnlyTheirVehicles() throws Exception {
        long customerId = createCustomer();
        mockMvc.perform(post("/api/v1/customers/" + customerId + "/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleBody("FLT1000")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/vehicles").param("customerId", String.valueOf(customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }
}
