package com.tenacious.ecommerce.product.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Product Service API")
                .description("Product catalog management for the E-Commerce Order Management System")
                .version("1.0.0")
                .contact(new Contact().name("Amol").email("amolsbrahme@gmail.com")));
    }
}
