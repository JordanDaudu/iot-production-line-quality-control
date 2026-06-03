package com.smartiot.qualityinspection.common.enums;

/**
 * Category of an alert raised by the system.
 */
public enum AlertType {
    /** A product was classified as FAIL. */
    FAILED_PRODUCT,
    /** A machine condition (temperature / vibration) needs maintenance. */
    MAINTENANCE,
    /** A virtual sensor went offline or recovered. */
    SENSOR_HEALTH
}
