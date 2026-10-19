package com.tenacious.ecommerce.order.exception;

/**
 * Raised by the Resilience4J circuit-breaker fallback when the Inventory
 * Service is unreachable, erroring, or the breaker is currently open -
 * i.e. an infrastructure fault, as opposed to a legitimate business
 * rejection ({@link InsufficientStockException}).
 */
public class InventoryServiceUnavailableException extends RuntimeException {
    public InventoryServiceUnavailableException(String sku, Throwable cause) {
        super("Inventory Service unavailable while reserving sku '%s': %s"
                .formatted(sku, cause.getMessage()), cause);
    }
}
