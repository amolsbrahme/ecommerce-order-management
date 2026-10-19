package com.tenacious.ecommerce.inventory.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Inventory Service API")
                .description("Stock level tracking and reservation for the E-Commerce Order Management System")
                .version("1.0.0"));
    }
}
