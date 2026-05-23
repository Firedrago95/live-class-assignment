package com.liveclass.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long aggregateId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    private int retryCount;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime nextRetryAt;

    @Builder
    public OutboxEvent(Long aggregateId, String payload) {
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.nextRetryAt = LocalDateTime.now();
    }

    public void markAsProcessing() {
        verifyNotTerminal();
        this.status = OutboxStatus.PROCESSING;
    }

    public void markAsSuccess() {
        verifyNotTerminal();
        this.status = OutboxStatus.SUCCESS;
    }

    public void processFailure(int maxRetries) {
        verifyNotTerminal();

        this.retryCount++;

        if (this.retryCount >= maxRetries) {
            this.status = OutboxStatus.FAILED;
        } else {
            long baseDelay = 2000L;
            long exponentialDelay = baseDelay * (1L << this.retryCount);
            long jitter = (long) (Math.random() * 1000);

            this.nextRetryAt = LocalDateTime.now().plus(Duration.ofMillis(exponentialDelay + jitter));
            this.status = OutboxStatus.PENDING;
        }
    }

    private void verifyNotTerminal() {
        if (this.status == OutboxStatus.SUCCESS || this.status == OutboxStatus.FAILED) {
            throw new IllegalStateException(
                "이미 종료된 상태(" + this.status + ")이므로 상태를 변경할 수 없습니다. (OutboxEvent ID: " + this.id + ")"
            );
        }
    }
}
