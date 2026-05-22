package com.liveclass.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications", uniqueConstraints = {
    @UniqueConstraint(name = "uk_source_event_receiver_type",
                      columnNames = {"source_event_id", "receiver_id", "type"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="source_event_id", nullable = false)
    private String sourceEventId;

    @Column(name = "receiver_id", nullable = false)
    private String receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Notification(String sourceEventId, String receiver, NotificationType type, NotificationChannel channel) {
        this.sourceEventId = sourceEventId;
        this.receiver = receiver;
        this.type = type;
        this.channel = channel;
        this.createdAt = LocalDateTime.now();
    }
}
