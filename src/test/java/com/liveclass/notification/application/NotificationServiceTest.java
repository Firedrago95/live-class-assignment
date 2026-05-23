package com.liveclass.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.liveclass.notification.domain.Notification;
import com.liveclass.notification.domain.NotificationChannel;
import com.liveclass.notification.domain.NotificationType;
import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.domain.OutboxStatus;
import com.liveclass.notification.infrastructure.persistence.NotificationRepository;
import com.liveclass.notification.infrastructure.persistence.OutboxEventRepository;
import com.liveclass.notification.presentation.dto.NotificationSendRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void 알림_등록_요청_시_Notification과_OutboxEvent가_각각_저장되어야_한다() {
        // given
        NotificationSendRequest request = new NotificationSendRequest(
            "source-1", "user-1", NotificationType.PAYMENT_SUCCESS, NotificationChannel.EMAIL, "payload"
        );

        Notification savedNotification = Notification.builder()
            .sourceEventId(request.sourceEventId())
            .receiver(request.receiverId())
            .type(request.type())
            .channel(request.channel())
            .build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // when
        notificationService.registerNotification(request);

        // then
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(outboxEventRepository, times(1)).save(any(OutboxEvent.class));
    }

    @Test
    void 대기중인_이벤트를_가져와서_상태를_PROCESSING으로_변경한다() {
        // given
        OutboxEvent event = new OutboxEvent(1L, "payload");

        when(outboxEventRepository.findPendingEventsForUpdate(any(LocalDateTime.class), any(LocalDateTime.class), eq(50)))
            .thenReturn(List.of(event));

        // when
        List<OutboxEvent> claimedEvents = notificationService.fetchAndClaimPendingEvents(50);

        // then
        assertThat(claimedEvents).hasSize(1);
        assertThat(claimedEvents.get(0).getStatus()).isEqualTo(OutboxStatus.PROCESSING);
    }

    @Test
    void 발송_성공_처리_시_이벤트_상태가_SUCCESS로_변경된다() {
        // given
        OutboxEvent event = new OutboxEvent(1L, "payload");
        event.markAsProcessing();
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(event));

        // when
        notificationService.processSuccess(1L);

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.SUCCESS);
    }

    @Test
    void 발송_실패_처리_시_재시도_카운트가_증가하고_PENDING_상태로_돌아간다() {
        // given
        OutboxEvent event = new OutboxEvent(1L, "payload");
        event.markAsProcessing();
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(event));

        // when
        notificationService.processFailure(1L, 3);

        // then
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }
}
