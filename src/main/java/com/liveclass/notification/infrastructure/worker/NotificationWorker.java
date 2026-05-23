package com.liveclass.notification.infrastructure.worker;

import com.liveclass.notification.application.NotificationService;
import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.infrastructure.external.ExternalNotificationClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWorker {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 3;

    private final NotificationService notificationService;
    private final ExternalNotificationClient externalClient;

    @Scheduled(fixedDelay = 1000)
    public void processPendingNotifications() {
        List<OutboxEvent> events = notificationService.fetchAndClaimPendingEvents(BATCH_SIZE);

        log.info("[Worker] {}개의 대기 중인 알림 발송을 시작합니다.", events.size());

        for (OutboxEvent event : events) {
            try {
                externalClient.send(event.getPayload());
                notificationService.processSuccess(event.getId());
            } catch (Exception e) {
                log.warn("[Worker] 알림 발송 실패. OutboxEventId: {}, 사유: {}", event.getId(), e.getMessage());
                notificationService.processFailure(event.getId(), MAX_RETRIES);
            }
        }
    }
}
