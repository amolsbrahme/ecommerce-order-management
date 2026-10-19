package com.tenacious.ecommerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
        @NotBlank(message = "sku must not be blank")
        String sku,

        @NotNull(message = "availableQuantity is required")
        @Min(value = 0, message = "availableQuantity cannot be negative")
        Integer availableQuantity
) {
}
