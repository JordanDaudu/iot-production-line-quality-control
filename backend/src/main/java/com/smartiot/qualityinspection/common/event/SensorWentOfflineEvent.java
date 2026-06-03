package com.smartiot.qualityinspection.common.event;

import com.smartiot.qualityinspection.sensor.model.Sensor;

/**
 * Published when a sensor is detected as offline (no readings within the threshold). A
 * listener turns this into a sensor-health alert, so the health service stays free of any
 * dependency on the alert module.
 */
public record SensorWentOfflineEvent(Sensor sensor) {
}
