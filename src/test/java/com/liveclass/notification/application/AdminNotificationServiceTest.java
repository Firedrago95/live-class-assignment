package com.liveclass.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.domain.OutboxStatus;
import com.liveclass.notification.infrastructure.persistence.OutboxEventRepository;
import com.liveclass.notification.presentation.dto.FailedOutboxEventResponse;
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
class AdminNotificationServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private AdminNotificationService adminNotificationService;

    @Test
    void 실패한_알림_목록_조회_시_DTO_리스트를_반환한다() {
        // given
        OutboxEvent event = mock(OutboxEvent.class);
        when(event.getStatus()).thenReturn(OutboxStatus.FAILED);

        when(outboxEventRepository.findByStatus(OutboxStatus.FAILED))
            .thenReturn(List.of(event));

        // when
        List<FailedOutboxEventResponse> results = adminNotificationService.getFailedNotifications();

        // then
        assertThat(results).hasSize(1);
    }

    @Test
    void 알림_재시도_요청_시_이벤트를_찾지_못하면_예외를_던진다() {
        // given
        Long eventId = 1L;
        when(outboxEventRepository.findById(eventId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminNotificationService.retryFailedNotification(eventId))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 알림_재시도_요청_시_성공적으로_상태를_변경한다() {
        // given
        Long eventId = 1L;
        OutboxEvent event = mock(OutboxEvent.class);
        when(outboxEventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // when
        adminNotificationService.retryFailedNotification(eventId);

        // then
        verify(event).manualRetry();
    }
}
