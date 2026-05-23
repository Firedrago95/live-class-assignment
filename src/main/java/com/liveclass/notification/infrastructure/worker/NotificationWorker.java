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
                boolean isSuccess = externalClient.send(event.getPayload());

                if (isSuccess) {
                    notificationService.processSuccess(event.getId());
                } else {
                    log.warn("[Worker] 외부 API 응답 실패. OutboxEventId: {}", event.getId());
                    notificationService.processFailure(event.getId(), MAX_RETRIES);
                }
            } catch (Exception e) {
                log.error("[Worker] 알림 발송 중 예외 발생. 사유: {}", e.getMessage());
                notificationService.processFailure(event.getId(), MAX_RETRIES);
            }
        }
    }
}
