package com.liveclass.notification.domain;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    SUCCESS,
    FAILED,
    DEAD_LETTER
}
