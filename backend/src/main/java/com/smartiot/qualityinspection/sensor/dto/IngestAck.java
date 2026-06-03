package com.smartiot.qualityinspection.sensor.dto;

/**
 * Acknowledgement returned to sensor clients on {@code /topic/ingest-ack} after a reading
 * is submitted over WebSocket (FR-08). Identifies whether the packet was accepted and, if
 * not, why.
 */
public record IngestAck(
        String sensorKey,
        boolean accepted,
        String error
) {
}
