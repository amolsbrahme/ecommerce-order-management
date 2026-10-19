package com.tenacious.ecommerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Order Service - places orders and owns the order lifecycle. Synchronously
 * verifies and reserves stock with the Inventory Service via a declarative
 * HTTP interface client backed by a load-balanced RestClient (see
 * {@link com.tenacious.ecommerce.order.client.InventoryClientConfig}),
 * guarded by a Resilience4J circuit breaker + retry, then asynchronously
 * publishes an order-placed event to Kafka for the Notification Service.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
