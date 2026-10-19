package com.tenacious.ecommerce.order.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Wires the declarative {@link InventoryClient} HTTP interface to a
 * load-balanced {@link RestClient}. "inventory-service" (from the
 * {@code inventory-service.url} property) is the logical Eureka service id,
 * not a real host - the {@code @LoadBalanced} builder bean below resolves
 * it to a real instance URI at call time via Spring Cloud LoadBalancer.
 */
@Configuration
public class InventoryClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(3000);
        return RestClient.builder().requestFactory(requestFactory);
    }

    @Bean
    public InventoryClient inventoryClient(RestClient.Builder loadBalancedRestClientBuilder,
                                            @Value("${inventory-service.url}") String inventoryServiceUrl) {
        RestClient restClient = loadBalancedRestClientBuilder.baseUrl(inventoryServiceUrl).build();
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return proxyFactory.createClient(InventoryClient.class);
    }
}