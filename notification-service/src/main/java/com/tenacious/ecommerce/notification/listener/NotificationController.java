package com.tenacious.ecommerce.notification.listener;

import com.tenacious.ecommerce.notification.model.Notification;
import com.tenacious.ecommerce.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Read-only view of recently sent notifications (for demo purposes)")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "List the most recent notifications sent by this service instance")
    public ResponseEntity<List<Notification>> getRecent() {
        return ResponseEntity.ok(notificationService.getRecent());
    }
}
