package com.tenacious.ecommerce.order.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Order Service API")
                .description("Order placement, stock reservation, and event publication for the "
                        + "E-Commerce Order Management System")
                .version("1.0.0"));
    }
}
