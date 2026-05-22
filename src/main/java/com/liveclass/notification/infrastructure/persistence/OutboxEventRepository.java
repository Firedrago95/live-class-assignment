package com.liveclass.notification.infrastructure.persistence;

import com.liveclass.notification.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

}
