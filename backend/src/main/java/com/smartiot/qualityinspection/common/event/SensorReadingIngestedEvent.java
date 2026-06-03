package com.smartiot.qualityinspection.common.event;

import com.smartiot.qualityinspection.sensor.model.SensorReading;

/**
 * Published after a sensor reading has been validated and stored. Listeners react to it
 * without the ingestion path needing to know about them (sensor health, maintenance
 * alerts, etc.), keeping the modules decoupled.
 */
public record SensorReadingIngestedEvent(SensorReading reading) {
}
