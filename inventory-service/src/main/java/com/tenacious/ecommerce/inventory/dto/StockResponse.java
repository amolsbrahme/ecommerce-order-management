package com.tenacious.ecommerce.inventory.dto;

import java.time.Instant;

public record StockResponse(
        Long id,
        String sku,
        Integer availableQuantity,
        Integer reservedQuantity,
        Instant updatedAt
) {
}
