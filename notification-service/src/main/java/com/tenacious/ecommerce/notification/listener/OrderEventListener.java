package com.tenacious.ecommerce.notification.listener;

import com.tenacious.ecommerce.notification.model.OrderPlacedEvent;
import com.tenacious.ecommerce.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${spring.kafka.topic.order-placed:order-placed-events}",
            containerFactory = "orderPlacedKafkaListenerContainerFactory"
    )
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received order-placed event for order {}", event.orderNumber());
        notificationService.notifyOrderPlaced(event);
    }
}
