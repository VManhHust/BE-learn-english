package com.example.belearnenglish.dto;

import java.time.Instant;
import java.util.Map;

public record NotificationItemResponse(
        Long id,
        String type,
        String priority,
        Map<String, Object> data,
        String actionUrl,
        boolean unread,
        Instant readAt,
        Instant createdAt,
        Instant expiresAt
) {
}
