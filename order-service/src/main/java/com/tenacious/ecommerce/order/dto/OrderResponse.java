package com.tenacious.ecommerce.order.dto;

import com.tenacious.ecommerce.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        String customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        String failureReason,
        List<OrderItemResponse> items,
        Instant createdAt
) {
}
