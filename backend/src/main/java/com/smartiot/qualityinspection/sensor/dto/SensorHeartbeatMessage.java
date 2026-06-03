package com.smartiot.qualityinspection.sensor.dto;

import com.smartiot.qualityinspection.common.enums.SensorType;

import java.time.Instant;

/**
 * Inbound heartbeat a sensor sends to report that it is alive (FR-17). Received on
 * {@code /app/sensor-heartbeat}.
 */
public record SensorHeartbeatMessage(
        String sensorKey,
        SensorType sensorType,
        Instant timestamp
) {
}
