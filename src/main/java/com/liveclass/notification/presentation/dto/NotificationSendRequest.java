package com.liveclass.notification.presentation.dto;

import com.liveclass.notification.domain.NotificationChannel;
import com.liveclass.notification.domain.NotificationType;

public record NotificationSendRequest(
    String sourceEventId,
    String receiverId,
    NotificationType type,
    NotificationChannel channel,
    String payload
) {

}
