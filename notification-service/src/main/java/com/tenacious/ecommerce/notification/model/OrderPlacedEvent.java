package com.tenacious.ecommerce.notification.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Notification Service's own copy of the order-placed event contract
 * published by the Order Service. Field names/shape must stay in sync with
 * {@code com.tenacious.ecommerce.order.event.OrderPlacedEvent} - documented as
 * the wire contract for the {@code order-placed-events} Kafka topic in the
 * project report.
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
