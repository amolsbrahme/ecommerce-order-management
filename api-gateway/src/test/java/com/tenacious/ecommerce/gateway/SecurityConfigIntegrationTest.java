package com.tenacious.ecommerce.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Verifies the gateway's authorization rules in isolation from routing:
 * public product reads are open, everything else requires a valid JWT with
 * the right realm role. The downstream {@code lb://...} routes themselves
 * are not exercised here (that needs a running Eureka + backing service),
 * so a request that passes the security filter chain is expected to fail
 * later at routing (404/502/503) inside this narrow test slice - only the
 * security response codes (401/403 vs "not blocked by security") are
 * asserted.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class SecurityConfigIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    @Test
    void productCatalogRead_isNotBlockedBySecurity_withoutAuthentication() {
        HttpStatusCode status = webTestClient.get().uri("/api/products")
                .exchange()
                .returnResult(Void.class)
                .getStatus();

        // No backing route/service exists in this narrow test slice, so the
        // exact status varies (404/502/503) - what matters is that security
        // did NOT reject it with 401/403.
        assertThat(status.value()).isNotIn(401, 403);
    }

    @Test
    void placingOrder_withoutToken_isRejectedAsUnauthorized() {
        webTestClient.post().uri("/api/orders")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void mutatingProductCatalog_withCustomerRoleOnly_isForbidden() {
        when(jwtDecoder.decode(eq("customer-token"))).thenReturn(Mono.just(customerJwt()));

        webTestClient.post().uri("/api/products")
                .header("Authorization", "Bearer customer-token")
                .exchange()
                .expectStatus().isForbidden();
    }

    private Jwt customerJwt() {
        return Jwt.withTokenValue("customer-token")
                .header("alg", "none")
                .claim("sub", "customer-1")
                .claim("realm_access", Map.of("roles", List.of("CUSTOMER")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
