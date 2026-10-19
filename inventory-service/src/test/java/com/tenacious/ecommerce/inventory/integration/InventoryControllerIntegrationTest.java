package com.tenacious.ecommerce.inventory.integration;

import com.tenacious.ecommerce.inventory.dto.StockAdjustmentRequest;
import com.tenacious.ecommerce.inventory.dto.StockRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test against a real MySQL instance (Testcontainers), also
 * exercising the Flyway migrations in {@code db/migration} - if the
 * migrations don't apply cleanly, this test fails at context startup.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class InventoryControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.cloud.config.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createReserveAndFetch_roundTripsThroughRealDatabase() throws Exception {
        StockRequest createRequest = new StockRequest("SKU-IT-1", 20);
        mockMvc.perform(post("/api/inventory")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.availableQuantity").value(20));

        StockAdjustmentRequest reserveRequest = new StockAdjustmentRequest(5);
        mockMvc.perform(post("/api/inventory/{sku}/reserve", "SKU-IT-1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reserveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(15))
                .andExpect(jsonPath("$.reservedQuantity").value(5));

        mockMvc.perform(get("/api/inventory/{sku}", "SKU-IT-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(15));
    }

    @Test
    void reserve_returnsConflict_whenStockInsufficient() throws Exception {
        StockRequest createRequest = new StockRequest("SKU-IT-2", 1);
        mockMvc.perform(post("/api/inventory")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        StockAdjustmentRequest reserveRequest = new StockAdjustmentRequest(99);
        mockMvc.perform(post("/api/inventory/{sku}/reserve", "SKU-IT-2")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(reserveRequest)))
                .andExpect(status().isConflict());
    }
}
