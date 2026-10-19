package com.tenacious.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Product Service - owns the product catalog. Business capability boundary:
 * create and retrieve product entries. Persists to a dedicated MongoDB
 * database (database-per-service), chosen because the product catalog is a
 * document-shaped, schema-flexible read model well suited to MongoDB.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
