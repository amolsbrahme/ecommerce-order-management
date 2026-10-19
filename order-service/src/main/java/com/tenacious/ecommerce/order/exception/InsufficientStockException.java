package com.tenacious.ecommerce.order.exception;

/**
 * The Inventory Service rejected a reservation because there wasn't enough
 * available stock (HTTP 409 from Inventory Service). This is a normal
 * business outcome, not an infrastructure fault - deliberately excluded
 * from Resilience4J's failure/retry accounting (see order-service.yml).
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String sku, int requestedQuantity) {
        super("Insufficient stock for sku '%s' (requested %d)".formatted(sku, requestedQuantity));
    }
}
