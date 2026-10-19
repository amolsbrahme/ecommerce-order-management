package com.tenacious.ecommerce.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Inventory Service - the single source of truth for stock levels. Order
 * Service calls into this service synchronously (via a declarative HTTP
 * interface client backed by RestClient, guarded by a circuit breaker) to
 * reserve stock before an order is confirmed.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
