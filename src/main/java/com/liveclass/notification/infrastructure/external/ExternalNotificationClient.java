package com.liveclass.notification.infrastructure.external;

public interface ExternalNotificationClient {

    boolean send(String payload);
}
