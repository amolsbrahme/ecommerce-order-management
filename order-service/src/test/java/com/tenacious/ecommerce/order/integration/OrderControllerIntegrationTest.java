package com.tenacious.ecommerce.order.integration;

import com.tenacious.ecommerce.order.client.InventoryGateway;
import com.tenacious.ecommerce.order.client.InventoryStockResponse;
import com.tenacious.ecommerce.order.dto.OrderItemRequest;
import com.tenacious.ecommerce.order.dto.OrderRequest;
import com.tenacious.ecommerce.order.event.OrderEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test against a real MySQL instance (Testcontainers) to
 * validate the Flyway migrations and JPA mapping. The Inventory Service
 * and Kafka broker are mocked out at the {@link InventoryGateway} /
 * {@link OrderEventPublisher} seams, keeping this test focused on the
 * Order Service's own persistence behaviour rather than requiring the
 * whole distributed system to be running.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class OrderControllerIntegrationTest {

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

    @MockBean
    private InventoryGateway inventoryGateway;

    @MockBean
    private OrderEventPublisher orderEventPublisher;

    @Test
    void placeOrder_persistsConfirmedOrder_whenInventoryReservationSucceeds() throws Exception {
        when(inventoryGateway.reserveStock(anyString(), anyInt()))
                .thenReturn(new InventoryStockResponse(1L, "SKU-100", 10, 2));

        OrderRequest request = new OrderRequest("customer-IT-1", List.of(
                new OrderItemRequest("SKU-100", 2, new BigDecimal("10.00"))));

        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(20.00));
    }
}
