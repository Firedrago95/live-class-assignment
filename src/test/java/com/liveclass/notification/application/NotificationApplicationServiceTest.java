package com.liveclass.notification.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.liveclass.notification.presentation.dto.NotificationSendRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationApplicationService notificationApplicationService;

    @Test
    void 중복_요청_발생_시_예외를_잡아_흐름을_유지한다() {
        // given
        NotificationSendRequest request = mock(NotificationSendRequest.class);
        doThrow(new DataIntegrityViolationException("Duplicate"))
            .when(notificationService).registerNotification(request);

        // when
        notificationApplicationService.send(request);

        // then
        verify(notificationService, times(1)).registerNotification(request);
    }
}
