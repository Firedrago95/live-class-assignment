package com.liveclass.notification.application;

import com.liveclass.notification.domain.Notification;
import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.infrastructure.persistence.NotificationRepository;
import com.liveclass.notification.infrastructure.persistence.OutboxEventRepository;
import com.liveclass.notification.presentation.dto.NotificationSendRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    public static final int TIMEOUT_LIMIT = 5;

    private final NotificationRepository notificationRepository;
    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void registerNotification(NotificationSendRequest request) {
        Notification notification = Notification.builder()
            .sourceEventId(request.sourceEventId())
            .receiver(request.receiverId())
            .type(request.type())
            .channel(request.channel())
            .build();

        Notification savedNotification = notificationRepository.save(notification);

        OutboxEvent outboxEvent = OutboxEvent.builder()
            .aggregateId(savedNotification.getId())
            .payload(request.payload())
            .build();

        outboxEventRepository.save(outboxEvent);
    }

    @Transactional
    public List<OutboxEvent> fetchAndClaimPendingEvents(int batchSize) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeout = now.minusMinutes(TIMEOUT_LIMIT);

        List<OutboxEvent> events = outboxEventRepository.findPendingEventsForUpdate(now, timeout, batchSize);

        for (OutboxEvent event : events) {
            event.markAsProcessing();
        }
        return events;
    }

    @Transactional
    public void processSuccess(Long eventId) {
        outboxEventRepository.findById(eventId).ifPresent(OutboxEvent::markAsSuccess);
    }

    @Transactional
    public void processFailure(Long eventId, int maxRetries) {
        outboxEventRepository.findById(eventId).ifPresent(event -> event.processFailure(maxRetries));
    }
}
