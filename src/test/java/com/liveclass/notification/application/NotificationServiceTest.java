package com.liveclass.notification.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.liveclass.notification.domain.Notification;
import com.liveclass.notification.domain.NotificationChannel;
import com.liveclass.notification.domain.NotificationType;
import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.infrastructure.persistence.NotificationRepository;
import com.liveclass.notification.infrastructure.persistence.OutboxEventRepository;
import com.liveclass.notification.presentation.dto.NotificationSendRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
}
