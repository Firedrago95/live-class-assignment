package com.liveclass.notification.infrastructure.worker;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.liveclass.notification.application.NotificationService;
import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.infrastructure.external.ExternalNotificationClient;
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
    private NotificationService notificationService;

    @Mock
    private ExternalNotificationClient externalClient;

    @InjectMocks
    private NotificationWorker notificationWorker;

    @Test
    void 외부_API_발송_성공_시_서비스의_성공_처리_메서드를_호출한다() {
        // given
        OutboxEvent event = mock(OutboxEvent.class);
        when(event.getId()).thenReturn(1L);
        when(event.getPayload()).thenReturn("test-payload");

        when(notificationService.fetchAndClaimPendingEvents(eq(50)))
            .thenReturn(List.of(event));

        when(externalClient.send("test-payload")).thenReturn(true);

        // when
        notificationWorker.processPendingNotifications();

        // then
        verify(externalClient, times(1)).send("test-payload");
        verify(notificationService, times(1)).processSuccess(1L);
    }

    @Test
    void 외부_API_발송이_실패_응답을_반환하면_서비스의_실패_처리_메서드를_호출한다() {
        // given
        OutboxEvent event = mock(OutboxEvent.class);
        when(event.getId()).thenReturn(1L);
        when(event.getPayload()).thenReturn("test-payload");

        when(notificationService.fetchAndClaimPendingEvents(eq(50)))
            .thenReturn(List.of(event));

        when(externalClient.send("test-payload")).thenReturn(false);

        // when
        notificationWorker.processPendingNotifications();

        // then
        verify(externalClient, times(1)).send("test-payload");
        verify(notificationService, times(1)).processFailure(1L, 3);
    }

    @Test
    void 외부_API_발송_예외_발생_시_서비스의_실패_처리_메서드를_호출한다() {
        // given
        OutboxEvent event = mock(OutboxEvent.class);
        when(event.getId()).thenReturn(1L);
        when(event.getPayload()).thenReturn("test-payload");

        when(notificationService.fetchAndClaimPendingEvents(eq(50)))
            .thenReturn(List.of(event));
        when(externalClient.send(any())).thenThrow(new RuntimeException("External API Down"));

        // when
        notificationWorker.processPendingNotifications();

        // then
        verify(notificationService, times(1)).processFailure(1L, 3);
    }
}
