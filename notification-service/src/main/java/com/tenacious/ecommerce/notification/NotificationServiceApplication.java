package com.tenacious.ecommerce.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Notification Service - a stateless Kafka consumer. Listens for
 * order-placed events published by the Order Service and reacts by
 * sending a notification. Deliberately has no database of its own: it
 * exists purely to demonstrate the asynchronous, event-driven half of the
 * architecture (as opposed to the synchronous Order -> Inventory call).
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
