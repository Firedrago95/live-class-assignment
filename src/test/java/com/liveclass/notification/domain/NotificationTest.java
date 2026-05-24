package com.liveclass.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationTest {

    @Test
    void 알림을_읽음_상태로_변경한다() {
        // given
        Notification notification = Notification.builder()
            .sourceEventId("event-1")
            .receiver("user-1")
            .type(NotificationType.PAYMENT_SUCCESS)
            .channel(NotificationChannel.EMAIL)
            .isRead(false)
            .build();

        // when
        notification.markAsRead();

        // then
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void 이미_읽은_상태에서_다시_읽음_처리해도_상태는_유지된다() {
        // given
        Notification notification = Notification.builder()
            .isRead(true)
            .build();

        // when
        notification.markAsRead();

        // then
        assertThat(notification.isRead()).isTrue();
    }
}
