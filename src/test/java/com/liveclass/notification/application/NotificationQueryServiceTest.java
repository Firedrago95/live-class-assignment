package com.liveclass.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.infrastructure.persistence.NotificationRepository;
import com.liveclass.notification.infrastructure.persistence.OutboxEventRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationQueryServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationQueryService notificationQueryService;

    @Test
    void 알림_이벤트의_현재_상태를_정상적으로_조회한다() {
        // given
        OutboxEvent event = OutboxEvent.builder().build();
        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(event));

        // when
        String status = notificationQueryService.getNotificationStatus(1L);

        // then
        assertThat(status).isEqualTo("PENDING");
    }

    @Test
    void 읽음_필터가_없을_때_사용자_전체_알림을_조회한다() {
        // when
        notificationQueryService.getUserNotifications("user1", null, Pageable.unpaged());

        // then
        verify(notificationRepository).findByReceiverId("user1", Pageable.unpaged());
        verifyNoMoreInteractions(notificationRepository);
    }
}
