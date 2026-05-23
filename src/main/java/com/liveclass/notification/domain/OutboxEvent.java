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

    @Column(columnDefinition = "TEXT")
    private String failureReason;

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

    public void processFailure(int maxRetries, String reason) {
        verifyNotTerminal();

        this.retryCount++;
        this.failureReason = reason;

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

    public void manualRetry() {
        if (this.status != OutboxStatus.FAILED && this.status != OutboxStatus.DEAD_LETTER) {
            throw new IllegalStateException("수동 재시도는 FAILED 또는 DEAD_LETTER 상태에서만 가능합니다. 현재 상태: " + this.status);
        }

        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.nextRetryAt = LocalDateTime.now();

        if (this.failureReason != null) {
            this.failureReason = "[수동 재시도됨] " + this.failureReason;
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
