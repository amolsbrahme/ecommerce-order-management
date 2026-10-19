package com.tenacious.ecommerce.notification.service;

import com.tenacious.ecommerce.notification.model.Notification;
import com.tenacious.ecommerce.notification.model.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Sends the actual notification. In this capstone scope, "sending" means
 * structured logging plus keeping the most recent notifications in memory
 * so the {@code GET /api/notifications} endpoint has something to show
 * during a demo - a real deployment would swap this for an email/SMS/push
 * provider integration behind the same interface.
 */
@Service
@Slf4j
public class NotificationService {

    private static final int MAX_HISTORY = 100;

    private final LinkedList<Notification> recentNotifications = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void notifyOrderPlaced(OrderPlacedEvent event) {
        String message = "Your order %s for %s has been confirmed (%d item(s))."
                .formatted(event.orderNumber(), event.totalAmount(), event.items().size());

        Notification notification = new Notification(event.orderNumber(), event.customerId(), message, Instant.now());

        log.info("NOTIFICATION -> customer={} order={} message=\"{}\"",
                event.customerId(), event.orderNumber(), message);

        lock.lock();
        try {
            recentNotifications.addFirst(notification);
            while (recentNotifications.size() > MAX_HISTORY) {
                recentNotifications.removeLast();
            }
        } finally {
            lock.unlock();
        }
    }

    public List<Notification> getRecent() {
        lock.lock();
        try {
            return Collections.unmodifiableList(new LinkedList<>(recentNotifications));
        } finally {
            lock.unlock();
        }
    }
}
