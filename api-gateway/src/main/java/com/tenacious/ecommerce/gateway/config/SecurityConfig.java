package com.tenacious.ecommerce.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Enforces authentication and role-based authorization at the API Gateway,
 * the single external entry point into the system. Downstream services
 * are not individually re-secured with OAuth2 in this iteration; the
 * gateway is treated as the trust boundary ("edge security"), which is a
 * deliberate, documented scope decision for this capstone (see the
 * project report's design trade-offs section) rather than an oversight -
 * token relay / mTLS between services is listed as future work.
 *
 * <p>The {@link org.springframework.security.oauth2.jwt.ReactiveJwtDecoder}
 * used to validate incoming bearer tokens is auto-configured by Spring
 * Boot directly from {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}
 * (this module's own {@code application.yml}, overridable via the
 * {@code KEYCLOAK_ISSUER_URI} environment variable) - it fetches Keycloak's
 * OpenID Connect discovery document and JWK set at startup, so no decoder
 * bean needs to be defined here explicitly.</p>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        // Infra/observability endpoints are open.
                        .pathMatchers("/actuator/**").permitAll()
                        // Public product catalog browsing.
                        .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        // Product catalog mutation is admin-only.
                        .pathMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        // Placing/viewing orders requires an authenticated customer or admin.
                        .pathMatchers("/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                        // Inventory management (via the gateway) is admin-only. Internal
                        // Order -> Inventory reservation calls bypass the gateway entirely.
                        .pathMatchers("/api/inventory/**").hasRole("ADMIN")
                        .pathMatchers("/api/notifications/**").hasAnyRole("CUSTOMER", "ADMIN")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return http.build();
    }

    private ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
