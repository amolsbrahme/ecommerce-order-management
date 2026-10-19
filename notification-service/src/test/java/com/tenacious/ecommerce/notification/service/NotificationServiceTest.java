package com.tenacious.ecommerce.notification.service;

import com.tenacious.ecommerce.notification.model.OrderPlacedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationServiceTest {

    @Test
    void notifyOrderPlaced_recordsNotificationInRecentHistory() {
        NotificationService service = new NotificationService();
        OrderPlacedEvent event = new OrderPlacedEvent(
                "ORD-123", "customer-1", new BigDecimal("25.00"),
                List.of(new OrderPlacedEvent.OrderedItem("SKU-100", 2, new BigDecimal("10.00"))),
                Instant.now());

        service.notifyOrderPlaced(event);

        assertThat(service.getRecent()).hasSize(1);
        assertThat(service.getRecent().get(0).orderNumber()).isEqualTo("ORD-123");
        assertThat(service.getRecent().get(0).message()).contains("ORD-123");
    }

    @Test
    void getRecent_caps_atMostOneHundredEntries() {
        NotificationService service = new NotificationService();
        for (int i = 0; i < 150; i++) {
            OrderPlacedEvent event = new OrderPlacedEvent(
                    "ORD-" + i, "customer-1", BigDecimal.ONE, List.of(), Instant.now());
            service.notifyOrderPlaced(event);
        }

        assertThat(service.getRecent()).hasSize(100);
        // Most recent first.
        assertThat(service.getRecent().get(0).orderNumber()).isEqualTo("ORD-149");
    }
}
