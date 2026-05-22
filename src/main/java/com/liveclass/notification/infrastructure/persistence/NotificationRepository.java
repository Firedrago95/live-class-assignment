package com.liveclass.notification.infrastructure.persistence;

import com.liveclass.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

}
