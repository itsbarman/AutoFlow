package com.autoflow.workorder;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
class WorkOrderLineControllerIntegrationTest extends PostgresContainerSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long setupWorkOrder() throws Exception {
        String customer = mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ola","lastName":"Nordmann","phoneNumber":"12345678"}
                                """))
                .andReturn().getResponse().getContentAsString();
        long customerId = objectMapper.readTree(customer).get("id").asLong();

        String vehicle = mockMvc.perform(post("/api/v1/customers/" + customerId + "/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"registrationNumber":"LINE100","make":"Toyota","model":"Corolla","fuelType":"PETROL"}
                                """))
                .andReturn().getResponse().getContentAsString();
        long vehicleId = objectMapper.readTree(vehicle).get("id").asLong();

        String workOrder = mockMvc.perform(post("/api/v1/vehicles/" + vehicleId + "/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Bremseservice"}
                                """))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(workOrder).get("id").asLong();
    }

    @Test
    void addLine_computesLineTotal() throws Exception {
        long workOrderId = setupWorkOrder();

        mockMvc.perform(post("/api/v1/work-orders/" + workOrderId + "/lines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Bremseklosser","partNumber":"BP-123","quantity":2,"unitPrice":499.50}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lineTotal").value(999.00))
                .andExpect(jsonPath("$.workOrderId").value(workOrderId));

        mockMvc.perform(get("/api/v1/work-orders/" + workOrderId + "/lines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void addLine_withZeroQuantity_returns400() throws Exception {
        long workOrderId = setupWorkOrder();

        mockMvc.perform(post("/api/v1/work-orders/" + workOrderId + "/lines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Ugyldig","quantity":0,"unitPrice":100}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors[0].field").value("quantity"));
    }
}
