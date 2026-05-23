package com.liveclass.notification.application;

import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.domain.OutboxStatus;
import com.liveclass.notification.infrastructure.persistence.OutboxEventRepository;
import com.liveclass.notification.presentation.dto.FailedOutboxEventResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminNotificationService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional(readOnly = true)
    public List<FailedOutboxEventResponse> getFailedNotifications() {
        return outboxEventRepository.findByStatus(OutboxStatus.FAILED).stream()
            .map(FailedOutboxEventResponse::from)
            .toList();
    }


    @Transactional
    public void retryFailedNotification(Long eventId) {
        OutboxEvent event = outboxEventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("해당 이벤트를 찾을 수 없습니다. ID: " + eventId));

        event.manualRetry();
    }
}
