package com.tenacious.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        String id,
        String sku,
        String name,
        String description,
        String category,
        BigDecimal price,
        Instant createdAt,
        Instant updatedAt
) {
}
