package com.smartiot.qualityinspection.dashboard.dto;

/**
 * A single machine-telemetry sample (temperature + vibration) at one point in time,
 * used to seed the dashboard trend chart with the run's history.
 *
 * @param timestamp   ISO-8601 timestamp
 * @param temperature temperature reading at that time (may be null if missing)
 * @param vibration   vibration reading at that time (may be null if missing)
 */
public record TelemetryPointDto(
        String timestamp,
        Double temperature,
        Double vibration
) {
}
