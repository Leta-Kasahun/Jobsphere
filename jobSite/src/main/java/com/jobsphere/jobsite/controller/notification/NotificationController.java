package com.jobsphere.jobsite.controller.notification;

import com.jobsphere.jobsite.dto.notification.NotificationResponse;
import com.jobsphere.jobsite.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API for managing user notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    @Operation(summary = "Get user notifications")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @PathVariable UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable)
            .map(this::mapToResponse);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{userId}/unread-count")
    @Operation(summary = "Get unread notifications count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable UUID userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{userId}/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID userId, @PathVariable UUID notificationId) {
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}/mark-all-read")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}")
    @Operation(summary = "Create a test notification")
    public ResponseEntity<Void> createTestNotification(@PathVariable UUID userId, @RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "Test Notification");
        String message = request.getOrDefault("message", "This is a test notification");
        notificationService.createNotification(userId, title, message);
        return ResponseEntity.ok().build();
    }

    private NotificationResponse mapToResponse(com.jobsphere.jobsite.model.notification.Notification notification) {
        return new NotificationResponse(
            notification.getId(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getIsRead(),
            notification.getCreatedAt()
        );
    }
}
