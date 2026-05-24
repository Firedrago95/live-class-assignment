package com.liveclass.notification.presentation;

import com.liveclass.notification.application.NotificationApplicationService;
import com.liveclass.notification.presentation.dto.NotificationSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;

    @PostMapping
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationSendRequest request) {
        notificationApplicationService.send(request);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> readNotification(@PathVariable Long notificationId) {
        notificationApplicationService.readNotification(notificationId);
        return ResponseEntity.ok().build();
    }
}
