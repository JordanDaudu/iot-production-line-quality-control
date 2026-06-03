package com.smartiot.qualityinspection.common.event;

import com.smartiot.qualityinspection.sensor.model.Sensor;

/**
 * Published when an offline sensor starts sending readings again.
 */
public record SensorRecoveredEvent(Sensor sensor) {
}
