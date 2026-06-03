package com.smartiot.qualityinspection.simulation.dto;

import com.smartiot.qualityinspection.common.enums.FaultType;
import jakarta.validation.constraints.NotNull;

/**
 * Request to inject a simulated fault (FR-20).
 *
 * @param faultType       which fault to trigger (required)
 * @param sensorKey       target sensor for SENSOR_DISCONNECT (defaults to VIBRATION-1)
 * @param durationSeconds how long a disconnect lasts (defaults to 20s)
 */
public record FaultInjectionRequest(
        @NotNull FaultType faultType,
        String sensorKey,
        Integer durationSeconds
) {
}
