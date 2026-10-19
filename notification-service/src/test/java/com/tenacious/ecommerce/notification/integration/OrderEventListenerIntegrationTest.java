package com.tenacious.ecommerce.notification.integration;

import com.tenacious.ecommerce.notification.model.OrderPlacedEvent;
import com.tenacious.ecommerce.notification.service.NotificationService;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * End-to-end verification of the Kafka consumption path against a real
 * broker (Testcontainers) - publishes an {@link OrderPlacedEvent} the same
 * way the Order Service would and asserts the Notification Service's
 * listener picked it up and recorded it.
 */
@Testcontainers
@SpringBootTest
class OrderEventListenerIntegrationTest {

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

    @Autowired
    private NotificationService notificationService;

    private KafkaTemplate<String, OrderPlacedEvent> testProducer() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        ProducerFactory<String, OrderPlacedEvent> factory = new DefaultKafkaProducerFactory<>(config);
        return new KafkaTemplate<>(factory);
    }

    @Test
    void orderPlacedEvent_isConsumedAndRecordedAsNotification() {
        OrderPlacedEvent event = new OrderPlacedEvent(
                "ORD-KAFKA-IT-1", "customer-kafka-it", new BigDecimal("42.00"),
                List.of(new OrderPlacedEvent.OrderedItem("SKU-100", 1, new BigDecimal("42.00"))),
                Instant.now());

        testProducer().send("order-placed-events", event.orderNumber(), event);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(notificationService.getRecent())
                        .anyMatch(n -> n.orderNumber().equals("ORD-KAFKA-IT-1")));
    }
}
