package com.liveclass.notification.application;

import com.liveclass.notification.presentation.dto.NotificationSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    private final NotificationService notificationService;

    public void send(NotificationSendRequest request) {
        try {
            notificationService.registerNotification(request);
        } catch (DataIntegrityViolationException e) {
            log.info("이미 접수된 알람입니다. 무시합니다.");
        }
    }

    public void readNotification(Long notificationId) {
        notificationService.readNotification(notificationId);
    }
}
