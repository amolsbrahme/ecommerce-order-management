package com.tenacious.ecommerce.order.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final String topic;

    public OrderEventPublisher(KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate,
                                @Value("${spring.kafka.topic.order-placed:order-placed-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        // Keyed by orderNumber so all events for the same order land on the
        // same partition and are consumed in order.
        kafkaTemplate.send(topic, event.orderNumber(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order-placed event for order {}: {}",
                                event.orderNumber(), ex.toString());
                    } else {
                        log.info("Published order-placed event for order {} to partition {}",
                                event.orderNumber(), result.getRecordMetadata().partition());
                    }
                });
    }
}
