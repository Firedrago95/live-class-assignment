package com.liveclass.notification.infrastructure.persistence;

import com.liveclass.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
           SELECT n FROM Notification n
           WHERE n.receiver = :receiverId
           ORDER BY n.createdAt DESC
           """)
    Page<Notification> findByReceiverId(@Param("receiverId")String receiverId, Pageable pageable);

    @Query("""
          SELECT n FROM Notification n
          WHERE n.receiver = :receiverId
            AND n.isRead = :isRead
          ORDER BY n.createdAt DESC
          """)
    Page<Notification> findByReceiverIdAndIsRead(
        @Param("receiverId") String receiverId,
        @Param("isRead") boolean isRead,
        Pageable pageable
    );
}
