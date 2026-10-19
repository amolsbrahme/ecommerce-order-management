package com.tenacious.ecommerce.order.client;

public record InventoryStockResponse(Long id, String sku, Integer availableQuantity, Integer reservedQuantity) {
}
