package com.autoflow.workorder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
 * Full-stack API tests for work orders, including creation under a vehicle,
 * the status PATCH, and the "cannot delete a vehicle with work orders" rule.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class WorkOrderControllerIntegrationTest extends PostgresContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long createCustomer() throws Exception {
        String response = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ola","lastName":"Nordmann","phoneNumber":"12345678"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createVehicle(long customerId, String regNumber) throws Exception {
        String response = mockMvc.perform(post("/api/v1/customers/" + customerId + "/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registrationNumber":"%s","make":"Toyota","model":"Corolla","fuelType":"PETROL"}
                                """.formatted(regNumber)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private long createWorkOrder(long vehicleId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/vehicles/" + vehicleId + "/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Brake service","priority":"HIGH","mileageAtArrival":45000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workOrderNumber").exists())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    void createWorkOrder_underVehicle_returnsCreatedWithNumber() throws Exception {
        long customerId = createCustomer();
        long vehicleId = createVehicle(customerId, "WOC1000");
        createWorkOrder(vehicleId);
    }

    @Test
    void patchStatus_toCompleted_setsCompletedAt() throws Exception {
        long customerId = createCustomer();
        long vehicleId = createVehicle(customerId, "WOC2000");
        long workOrderId = createWorkOrder(vehicleId);

        mockMvc.perform(patch("/api/v1/work-orders/" + workOrderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COMPLETED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    void createWorkOrder_withBlankTitle_returns400() throws Exception {
        long customerId = createCustomer();
        long vehicleId = createVehicle(customerId, "WOC3000");

        mockMvc.perform(post("/api/v1/vehicles/" + vehicleId + "/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("title"));
    }

    @Test
    void deleteVehicle_withWorkOrders_returns409() throws Exception {
        long customerId = createCustomer();
        long vehicleId = createVehicle(customerId, "WOC4000");
        createWorkOrder(vehicleId);

        mockMvc.perform(delete("/api/v1/vehicles/" + vehicleId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("arbeidsordre")));
    }

    @Test
    void getWorkOrders_filteredByVehicle_returnsOnlyTheirWorkOrders() throws Exception {
        long customerId = createCustomer();
        long vehicleId = createVehicle(customerId, "WOC5000");
        createWorkOrder(vehicleId);

        mockMvc.perform(get("/api/v1/work-orders").param("vehicleId", String.valueOf(vehicleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }
}
