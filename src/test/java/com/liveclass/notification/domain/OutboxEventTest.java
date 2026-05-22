package com.liveclass.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OutboxEventTest {

    @Test
    void 재시도_시_지수_백오프_로직이_정상_작동하여_다음_재시도_시간이_미래로_밀린다() {
        OutboxEvent event = new OutboxEvent(1L, "test");
        LocalDateTime beforeRetry = event.getNextRetryAt();

        event.processFailure(3);

        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getNextRetryAt()).isAfter(beforeRetry);
    }

    @Test
    void 최대_재시도_횟수_초과_시_FAILED_상태로_전이된다() {
        OutboxEvent event = new OutboxEvent(1L, "test");

        event.processFailure(3);
        event.processFailure(3);
        event.processFailure(3);

        assertThat(event.getRetryCount()).isEqualTo(3);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
    }
}
