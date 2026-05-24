package com.liveclass.notification.infrastructure.persistence;

import com.liveclass.notification.domain.OutboxEvent;
import com.liveclass.notification.domain.OutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
                   SELECT * FROM outbox_events
                   WHERE (status = 'PENDING'AND next_retry_at <= :now) 
                      OR (status = 'PROCESSING' AND updated_at <= :timeout)
                   ORDER BY created_at ASC
                   LIMIT :limit
                   FOR UPDATE SKIP LOCKED
                   """, nativeQuery = true)
    List<OutboxEvent> findPendingEventsForUpdate(
        @Param("now") LocalDateTime now,
        @Param("timeout") LocalDateTime timeout,
        @Param("limit") int limit
    );

    @Query("""
           SELECT o FROM OutboxEvent o
           WHERE o.status = :outboxStatus
           ORDER BY o.updatedAt DESC
           """)
    List<OutboxEvent> findByStatus(@Param("outboxStatus") OutboxStatus outboxStatus);
}
