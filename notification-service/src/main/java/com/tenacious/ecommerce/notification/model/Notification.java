package com.tenacious.ecommerce.notification.model;

import java.time.Instant;

public record Notification(String orderNumber, String customerId, String message, Instant sentAt) {
}
