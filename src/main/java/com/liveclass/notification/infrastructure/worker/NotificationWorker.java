package com.liveclass.notification.infrastructure.worker;

import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.infrastructure.external.ExternalNotificationClient;
import com.liveclass.notification.infrastructure.persistence.OutboxEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorker {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;

    private final OutboxEventRepository outboxEventRepository;
    private final ExternalNotificationClient externalClient;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void processPendingNotifications() {
        List<OutboxEvent> events = outboxEventRepository.findPendingEventsForUpdate(LocalDateTime.now(), BATCH_SIZE);

        if (events.isEmpty()) {
            return;
        }

        log.info("[Worker] {}개의 대기 중인 알림 발송을 시작합니다.", events.size());

        for (OutboxEvent event : events) {
            try {
                externalClient.send(event.getPayload());
                event.markAsSuccess();
            } catch (Exception e) {
                log.warn("[Worker] 알림 발송 실패. OutboxEventId: {}, 사유: {}", event.getId(), e.getMessage());
                event.processFailure(MAX_RETRIES);
            }
        }
    }
}
