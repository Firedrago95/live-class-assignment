package com.liveclass.notification.infrastructure.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.domain.OutboxStatus;
import com.liveclass.notification.infrastructure.external.ExternalNotificationClient;
import com.liveclass.notification.infrastructure.persistence.OutboxEventRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationWorkerTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ExternalNotificationClient externalClient;

    @InjectMocks
    private NotificationWorker notificationWorker;

    @Test
    void 외부_API_발송_성공_시_OutboxEvent의_상태가_SUCCESS로_변경된다() {
        // given
        OutboxEvent event = new OutboxEvent(1L, "test");
        when(outboxEventRepository.findPendingEventsForUpdate(any(), eq(50)))
            .thenReturn(List.of(event));

        // when
        notificationWorker.processPendingNotifications();

        // then
        verify(externalClient, times(1)).send(any());
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.SUCCESS);
    }

    @Test
    void 외부_API_발송_예외_발생_시_재시도_카운트가_증가하고_실패_처리를_진행한다() {
        // given
        OutboxEvent event = new OutboxEvent(1L, "test");
        when(outboxEventRepository.findPendingEventsForUpdate(any(), eq(50)))
            .thenReturn(List.of(event));

        doThrow(new RuntimeException("External API Down")).when(externalClient).send(any());

        // when
        notificationWorker.processPendingNotifications();

        // then
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
    }
}
