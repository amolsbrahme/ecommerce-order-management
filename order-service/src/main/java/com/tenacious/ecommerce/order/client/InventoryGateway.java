package com.tenacious.ecommerce.order.client;

import com.tenacious.ecommerce.order.exception.InsufficientStockException;
import com.tenacious.ecommerce.order.exception.InventoryServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Wraps the raw {@link InventoryClient} HTTP interface calls with the
 * resilience patterns required on the Order -> Inventory call path: a
 * circuit breaker (trip fast once Inventory Service is clearly unhealthy,
 * instead of piling up slow/failing calls) and a bounded retry for
 * transient blips.
 *
 * <p>A 409 (insufficient stock) is translated to
 * {@link InsufficientStockException} <em>before</em> Resilience4J sees it,
 * and that type is configured as an "ignored" exception for both the
 * circuit breaker and the retry (see {@code order-service.yml}) - a
 * legitimate business rejection must never trip the breaker or be
 * retried.</p>
 *
 * <p><b>Important:</b> a circuit breaker's {@code ignore-exceptions} only
 * excludes an exception from failure-rate counting - it does
 * <em>not</em> skip the fallback method, which still runs for every
 * exception regardless. {@link #reserveFallback} must therefore rethrow an
 * already-ignored {@link InsufficientStockException} unchanged rather than
 * wrap it: if it didn't, {@code @Retry}'s own {@code ignore-exceptions}
 * check (the outer aspect here) would see the fallback's substitute
 * {@link InventoryServiceUnavailableException} instead of the real type,
 * and retry a rejection that should propagate immediately.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryGateway {

    private static final String INVENTORY_SERVICE = "inventoryService";

    private final InventoryClient inventoryClient;

    @CircuitBreaker(name = INVENTORY_SERVICE, fallbackMethod = "reserveFallback")
    @Retry(name = INVENTORY_SERVICE)
    public InventoryStockResponse reserveStock(String sku, int quantity) {
        try {
            return inventoryClient.reserve(sku, new InventoryStockRequest(quantity));
        } catch (HttpClientErrorException.Conflict conflict) {
            throw new InsufficientStockException(sku, quantity);
        }
    }

    @SuppressWarnings("unused") // invoked reflectively by Resilience4J
    private InventoryStockResponse reserveFallback(String sku, int quantity, Throwable throwable) {
        if (throwable instanceof InsufficientStockException insufficientStock) {
            // Rethrow unchanged - see the class Javadoc. The fallback runs
            // even for exceptions the circuit breaker is configured to
            // ignore, so this must not re-wrap an already-legitimate
            // business rejection into an infrastructure-fault type.
            throw insufficientStock;
        }
        log.warn("Inventory Service call failed for sku={}, quantity={}: {}", sku, quantity, throwable.toString());
        throw new InventoryServiceUnavailableException(sku, throwable);
    }

    /**
     * Best-effort compensating release of stock already reserved earlier
     * in the same order placement, used when a later item in the order
     * fails. Deliberately not wrapped in the circuit breaker: a failure
     * here must not mask the original failure that triggered the rollback,
     * it is only logged.
     */
    public void releaseStockBestEffort(String sku, int quantity) {
        try {
            inventoryClient.release(sku, new InventoryStockRequest(quantity));
        } catch (Exception ex) {
            log.error("Compensating release FAILED for sku={}, quantity={} - manual reconciliation may be "
                    + "required: {}", sku, quantity, ex.toString());
        }
    }
}
