package com.tenacious.ecommerce.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Notification Service API")
                .description("Consumes order-placed events from Kafka and sends notifications")
                .version("1.0.0"));
    }
}
