package com.liveclass.notification.presentation;

import com.liveclass.notification.application.AdminNotificationService;
import com.liveclass.notification.presentation.dto.FailedOutboxEventResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @GetMapping("/failed")
    public ResponseEntity<List<FailedOutboxEventResponse>> getFailedNotifications() {
        List<FailedOutboxEventResponse> failedEvents = adminNotificationService.getFailedNotifications();
        return ResponseEntity.ok(failedEvents);
    }

    @PostMapping("/retry/{eventId}")
    public ResponseEntity<Void> retryNotification(@PathVariable Long eventId) {
        adminNotificationService.retryFailedNotification(eventId);
        return ResponseEntity.ok().build();
    }
}
