package com.liveclass.notification.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.liveclass.notification.domain.Notification;
import com.liveclass.notification.domain.NotificationChannel;
import com.liveclass.notification.domain.NotificationType;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DisplayNameGeneration(ReplaceUnderscores.class)
class NotificationRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void 수신자_기준으로_최신순_알림_목록을_페이징_조회한다() {
        // given
        String receiverId = "user1";
        notificationRepository.save(Notification.builder()
            .sourceEventId("ev1")
            .receiver(receiverId)
            .type(NotificationType.PAYMENT_SUCCESS)
            .channel(NotificationChannel.EMAIL)
            .build());

        notificationRepository.save(Notification.builder()
            .sourceEventId("ev2")
            .receiver(receiverId)
            .type(NotificationType.PAYMENT_SUCCESS)
            .channel(NotificationChannel.EMAIL)
            .build());

        // when
        Page<Notification> result = notificationRepository.findByReceiverId(receiverId, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void 읽음_상태에_따라_필터링된다() {
        // given
        // given
        String receiverId = "user1";

        Notification readNotification = Notification.builder()
            .receiver(receiverId)
            .sourceEventId("ev1")
            .type(NotificationType.PAYMENT_SUCCESS)
            .channel(NotificationChannel.EMAIL)
            .isRead(true).build();

        Notification unreadNotification = Notification.builder()
            .receiver(receiverId)
            .sourceEventId("ev2")
            .type(NotificationType.PAYMENT_SUCCESS)
            .channel(NotificationChannel.EMAIL)
            .isRead(false).build();
        notificationRepository.saveAll(List.of(readNotification, unreadNotification));

        // when
        Page<Notification> unreadResult = notificationRepository.findByReceiverIdAndIsRead(receiverId, false, PageRequest.of(0, 10));

        // then
        assertThat(unreadResult.getContent()).hasSize(1);
        assertThat(unreadResult.getContent().get(0).getSourceEventId()).isEqualTo("ev2");
    }
}
