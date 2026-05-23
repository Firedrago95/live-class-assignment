package com.liveclass.notification.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.liveclass.notification.domain.OutboxEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DisplayNameGeneration(ReplaceUnderscores.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class OutboxEventRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void 동시_조회_시_SKIP_LOCKED_작동하여_중복_없이_데이터를_나누어_가져간다() throws InterruptedException {
        // given
        transactionTemplate.executeWithoutResult(status -> {
            outboxEventRepository.deleteAllInBatch();

            for (int i = 0; i < 10; i++) {
                outboxEventRepository.save(
                    OutboxEvent.builder()
                        .aggregateId((long) i)
                        .payload("test")
                        .build()
                );
            }
        });

        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger totalFetchedSize = new AtomicInteger(0);
        LocalDateTime targetTime = LocalDateTime.now().plusMinutes(1);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> {
                        List<OutboxEvent> events = outboxEventRepository.findPendingEventsForUpdate(targetTime, targetTime.minusMinutes(5), 5);
                        totalFetchedSize.addAndGet(events.size());

                        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    });
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        // then
        assertThat(totalFetchedSize.get()).isEqualTo(10);
        transactionTemplate.executeWithoutResult(status -> outboxEventRepository.deleteAllInBatch());
    }
}
