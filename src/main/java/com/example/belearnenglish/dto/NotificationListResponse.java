package com.example.belearnenglish.dto;

import java.util.List;

public record NotificationListResponse(
        List<NotificationItemResponse> items,
        long unreadCount,
        boolean hasMore
) {
}
