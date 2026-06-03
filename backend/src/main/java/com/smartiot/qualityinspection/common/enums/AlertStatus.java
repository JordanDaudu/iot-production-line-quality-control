package com.smartiot.qualityinspection.common.enums;

/**
 * Lifecycle state of an alert. Alerts remain ACTIVE until handled.
 */
public enum AlertStatus {
    ACTIVE,
    ACKNOWLEDGED,
    RESOLVED,
    /** Cleared by a demo reset/cleanup action. */
    CLEARED
}
