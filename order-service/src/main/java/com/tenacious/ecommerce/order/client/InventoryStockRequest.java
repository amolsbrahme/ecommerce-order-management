package com.tenacious.ecommerce.order.client;

/**
 * Order Service's own copy of the Inventory Service's reserve/release
 * request contract. Each microservice owns its own view of another
 * service's API rather than sharing a common DTO module - this keeps the
 * services independently deployable (a change to Inventory Service's
 * internal DTOs doesn't force a recompile of Order Service unless the
 * wire contract itself changes).
 */
public record InventoryStockRequest(Integer quantity) {
}
