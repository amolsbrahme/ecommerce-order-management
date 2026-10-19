package com.tenacious.ecommerce.product.integration;

import com.tenacious.ecommerce.product.dto.ProductRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end verification of the Product Service REST API against a real
 * MongoDB instance provisioned by Testcontainers - validates the full
 * request -> controller -> service -> repository -> database round trip,
 * not just mocked collaborators.
 *
 * <p>Requires a Docker daemon to be reachable from the machine running the
 * build (this is expected: the plan's testing strategy explicitly calls
 * for Testcontainers-backed integration tests).</p>
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ProductControllerIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // Belt-and-braces alongside @ServiceConnection for Spring Boot
        // versions/setups where auto service-connection isn't picked up.
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.cloud.config.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndFetchProduct_roundTripsThroughRealDatabase() throws Exception {
        ProductRequest request = new ProductRequest(
                "SKU-INTEGRATION-1", "USB-C Charger", "65W GaN charger",
                "Electronics", new BigDecimal("29.50"), 100);

        String responseBody = mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("SKU-INTEGRATION-1"))
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(responseBody).get("id").asText();

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("USB-C Charger"));
    }

    @Test
    void createProduct_rejectsInvalidPayload() throws Exception {
        String invalidJson = """
                {"sku": "", "name": "", "category": "", "price": -5}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
