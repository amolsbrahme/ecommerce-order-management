package com.tenacious.ecommerce.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway - the single entry point for external clients. Authenticates
 * callers against Keycloak (OAuth2 resource server / JWT validation) and
 * routes authenticated requests to the appropriate downstream service via
 * Eureka-based service discovery (see {@code api-gateway.yml} in the
 * config-repo for the route table).
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
