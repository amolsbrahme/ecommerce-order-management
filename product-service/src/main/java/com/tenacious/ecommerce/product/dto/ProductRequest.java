package com.tenacious.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Inbound payload for creating or updating a product. Kept separate from
 * the {@link com.tenacious.ecommerce.product.model.Product} entity so the
 * persistence model can evolve independently of the public API contract.
 */
public record ProductRequest(

        @NotBlank(message = "sku must not be blank")
        String sku,

        @NotBlank(message = "name must not be blank")
        String name,

        String description,

        @NotBlank(message = "category must not be blank")
        String category,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than 0")
        BigDecimal price,

        Integer initialStockHint
) {
}
