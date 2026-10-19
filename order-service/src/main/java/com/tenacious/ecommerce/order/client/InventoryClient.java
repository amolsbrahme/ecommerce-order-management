package com.tenacious.ecommerce.order.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Declarative HTTP client for the Inventory Service, built on Spring's own
 * HTTP Interface support and backed by a
 * load-balanced {@link org.springframework.web.client.RestClient} - see
 * {@link InventoryClientConfig} for how this interface is proxied to a real
 * implementation. The logical name "inventory-service" is resolved through
 * Eureka + Spring Cloud LoadBalancer at call time - no hardcoded host/port.
 */
@HttpExchange("/api/inventory")
public interface InventoryClient {

    @PostExchange("/{sku}/reserve")
    InventoryStockResponse reserve(@PathVariable("sku") String sku, @RequestBody InventoryStockRequest request);

    @PostExchange("/{sku}/release")
    InventoryStockResponse release(@PathVariable("sku") String sku, @RequestBody InventoryStockRequest request);
}