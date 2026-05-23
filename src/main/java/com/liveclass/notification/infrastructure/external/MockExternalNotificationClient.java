package com.liveclass.notification.infrastructure.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockExternalNotificationClient implements ExternalNotificationClient{

    @Override
    public boolean send(String payload) {
        log.info("[MockExternalClient] 📧 알림 발송 성공 (Payload: {})", payload);
        return true;
    }
}
