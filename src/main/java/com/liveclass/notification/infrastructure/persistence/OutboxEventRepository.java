package com.liveclass.notification.infrastructure.persistence;

import com.liveclass.notification.domain.OutboxEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
                   SELECT * FROM outbox_events
                   WHERE status = 'PENDING'
                     AND next_retry_at <= :now
                   ORDER BY created_at ASC
                   LIMIT :limit
                   FOR UPDATE SKIP LOCKED
                   """, nativeQuery = true)
    public List<OutboxEvent> findPendingEventsForUpdate(@Param("now") LocalDateTime now, @Param("limit") int limit);
}
