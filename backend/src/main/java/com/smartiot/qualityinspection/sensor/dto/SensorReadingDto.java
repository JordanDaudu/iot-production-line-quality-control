package com.smartiot.qualityinspection.sensor.dto;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.sensor.model.SensorReading;

/**
 * Outbound sensor reading, broadcast on {@code /topic/readings} and returned by queries.
 * Mirrors the frontend SensorReading type.
 *
 * <p>The timestamp is an ISO-8601 string rather than a temporal type: the STOMP message
 * converter does not register the Java-time module by default, so a String avoids
 * serialization surprises and matches the frontend contract directly.
 */
public record SensorReadingDto(
        Long id,
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
        String timestamp
) {

    public static SensorReadingDto from(SensorReading reading) {
        return new SensorReadingDto(
                reading.getId(),
                reading.getSensorType(),
                reading.getSensorKey(),
                reading.getProductCode(),
                reading.getMachineId(),
                reading.getBatchId(),
                reading.getSimulationRunId(),
                reading.getValue(),
                reading.getUnit(),
                reading.getDefectCategory(),
                reading.getConfidence(),
                reading.getTimestamp() != null ? reading.getTimestamp().toString() : null
        );
    }
}
