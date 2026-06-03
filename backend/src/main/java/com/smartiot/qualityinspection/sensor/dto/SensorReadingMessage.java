package com.smartiot.qualityinspection.sensor.dto;

import com.smartiot.qualityinspection.common.enums.SensorType;

import java.time.Instant;

/**
 * Inbound sensor reading payload. Both the in-process simulators and (in future) any
 * external WebSocket client submit this exact shape; it is validated by
 * {@code ReadingValidator} before anything is stored. This is the single ingestion
 * contract for the system.
 */
public record SensorReadingMessage(
        SensorType sensorType,
        String sensorKey,
        String productCode,
        String machineId,
        Long batchId,
        Long simulationRunId,
        Double value,
        String unit,
        String defectCategory,
        Double confidence,
        Instant timestamp
) {
}
