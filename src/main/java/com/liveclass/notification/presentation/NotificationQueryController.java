package com.liveclass.notification.presentation;

import com.liveclass.notification.application.NotificationQueryService;
import com.liveclass.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NotificationQueryController {

    private final NotificationQueryService notificationQueryService;

    @GetMapping("/notifications/outbox/{eventId}/status")
    public ResponseEntity<String> getNotificationStatus(@PathVariable Long eventId) {
        return ResponseEntity.ok(notificationQueryService.getNotificationStatus(eventId));
    }

    @GetMapping("/users/{userId}/notifications")
    public ResponseEntity<Page<Notification>> getUserNotifications(
        @PathVariable String userId,
        @RequestParam(required = false) Boolean isRead,
        Pageable pageable
    ) {
        return ResponseEntity.ok(notificationQueryService.getUserNotifications(userId, isRead, pageable));
    }
}
