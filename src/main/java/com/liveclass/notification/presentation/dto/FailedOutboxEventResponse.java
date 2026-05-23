package com.liveclass.notification.presentation.dto;

import com.liveclass.notification.domain.OutboxEvent;
import java.time.LocalDateTime;

public record FailedOutboxEventResponse(
    Long eventId,
    Long aggregateId,
    String payload,
    String status,
    int retryCount,
    String failureReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static FailedOutboxEventResponse from(OutboxEvent event) {
        return new FailedOutboxEventResponse(
            event.getId(),
            event.getAggregateId(),
            event.getPayload(),
            event.getStatus().name(),
            event.getRetryCount(),
            event.getFailureReason(),
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }
}
