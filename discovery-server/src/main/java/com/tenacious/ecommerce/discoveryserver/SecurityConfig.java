package com.tenacious.ecommerce.discoveryserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Protects the Eureka dashboard and registry endpoints with HTTP Basic auth
 * (credentials sourced from configuration) so the service registry is not
 * left wide open, while still allowing service-to-service registration
 * calls (which also present the same Basic credentials via
 * {@code eureka.client.service-url.defaultZone}).
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(basic -> { })
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
