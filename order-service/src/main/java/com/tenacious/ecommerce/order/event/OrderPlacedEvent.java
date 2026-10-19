package com.tenacious.ecommerce.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Published to the {@code order-placed-events} Kafka topic once an order
 * has been fully confirmed (all items reserved). The Notification Service
 * consumes this asynchronously to send an order confirmation - decoupling
 * "order placement" from "notify the customer" so a slow/unavailable
 * notification channel can never block or fail an order.
 */
public record OrderPlacedEvent(
        String orderNumber,
        String customerId,
        BigDecimal totalAmount,
        List<OrderedItem> items,
        Instant placedAt
) {
    public record OrderedItem(String sku, Integer quantity, BigDecimal unitPrice) {
    }
}
