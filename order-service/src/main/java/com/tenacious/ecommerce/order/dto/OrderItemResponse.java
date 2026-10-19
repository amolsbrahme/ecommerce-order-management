package com.tenacious.ecommerce.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(String sku, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
}
