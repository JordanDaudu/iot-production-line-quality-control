package com.smartiot.qualityinspection.sensor.dto;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.sensor.model.Sensor;

/**
 * Online/offline state of a virtual sensor, shown on the dashboard. Mirrors the frontend
 * SensorHealth type.
 */
public record SensorHealthDto(
        String sensorKey,
        SensorType sensorType,
        boolean online,
        String lastSeenAt
) {

    public static SensorHealthDto from(Sensor sensor) {
        return new SensorHealthDto(
                sensor.getSensorKey(),
                sensor.getSensorType(),
                sensor.isOnline(),
                sensor.getLastSeenAt() != null ? sensor.getLastSeenAt().toString() : null
        );
    }
}
