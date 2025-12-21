package com.jobsphere.jobsite.dto.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    String title,
    String message,
    Boolean isRead,
    Instant createdAt
) {}
