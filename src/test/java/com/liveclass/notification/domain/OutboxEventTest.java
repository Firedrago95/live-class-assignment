package com.liveclass.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OutboxEventTest {

    @Test
    void 이벤트를_PROCESSING_상태로_변경한다() {
        OutboxEvent event = new OutboxEvent(1L, "test");

        event.markAsProcessing();

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PROCESSING);
    }

    @Test
    void 재시도_시_지수_백오프_로직이_정상_작동하여_다음_재시도_시간이_미래로_밀리고_상태가_PENDING으로_돌아온다() {
        OutboxEvent event = new OutboxEvent(1L, "test");

        event.markAsProcessing();
        LocalDateTime beforeRetry = event.getNextRetryAt();

        // when
        event.processFailure(3, "Test failure");

        // then
        assertThat(event.getRetryCount()).isEqualTo(1);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getNextRetryAt()).isAfter(beforeRetry);
    }

    @Test
    void 최대_재시도_횟수_초과_시_FAILED_상태로_전이된다() {
        OutboxEvent event = new OutboxEvent(1L, "test");

        event.markAsProcessing();
        event.processFailure(3, "Test failure 1");

        event.markAsProcessing();
        event.processFailure(3, "Test failure 2");

        event.markAsProcessing();
        event.processFailure(3, "Test failure 3");

        // then
        assertThat(event.getRetryCount()).isEqualTo(3);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
    }

    @Test
    void FAILED_상태인_이벤트를_수동으로_재시도하면_PENDING_상태가_되고_횟수가_초기화된다() {
        // given
        OutboxEvent event = new OutboxEvent(1L, "test");
        event.markAsProcessing();
        event.processFailure(3, "Network Error");

        event.processFailure(3, "fail 2");
        event.processFailure(3, "fail 3");

        // when
        event.manualRetry();

        // then
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getRetryCount()).isZero();
        assertThat(event.getFailureReason()).contains("[수동 재시도됨]");
    }

    @Test
    void SUCCESS_상태인_이벤트를_수동_재시도하면_예외가_발생한다() {
        OutboxEvent event = new OutboxEvent(1L, "test");
        event.markAsProcessing();
        event.markAsSuccess();

        assertThatThrownBy(event::manualRetry)
            .isInstanceOf(IllegalStateException.class);
    }
}
