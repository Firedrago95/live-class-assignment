package com.liveclass.notification.infrastructure.external;

public interface ExternalNotificationClient {

    void send(String payload);
}
