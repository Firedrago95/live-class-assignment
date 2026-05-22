package com.liveclass.notification.application;

import com.liveclass.notification.domain.Notification;
import com.liveclass.notification.infrastructure.persistence.NotificationRepository;
import com.liveclass.notification.infrastructure.persistence.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final OutboxEventRepository outboxEventRepository;
    private final NotificationRepository notificationRepository;

    public String getNotificationStatus(Long eventId) {
        return outboxEventRepository.findById(eventId)
            .map(event -> event.getStatus().name())
            .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));
    }

    public Page<Notification> getUserNotifications(String receiverId, Boolean isRead, Pageable pageable) {
        if (isRead == null) {
            return notificationRepository.findByReceiverId(receiverId, pageable);
        }
        return notificationRepository.findByReceiverIdAndIsRead(receiverId, isRead, pageable);
    }
}
