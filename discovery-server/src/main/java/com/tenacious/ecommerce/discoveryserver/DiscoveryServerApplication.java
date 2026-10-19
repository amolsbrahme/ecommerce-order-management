package com.tenacious.ecommerce.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka service registry. Every business and infrastructure service
 * registers itself here on startup and queries it to discover the current
 * network location of the services it depends on, removing the need for
 * hard-coded hostnames/ports anywhere in the system.
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
