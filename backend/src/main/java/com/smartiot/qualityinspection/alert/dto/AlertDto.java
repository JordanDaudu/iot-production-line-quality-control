package com.smartiot.qualityinspection.alert.dto;

import com.smartiot.qualityinspection.alert.model.Alert;
import com.smartiot.qualityinspection.common.enums.AlertSeverity;
import com.smartiot.qualityinspection.common.enums.AlertStatus;
import com.smartiot.qualityinspection.common.enums.AlertType;

/**
 * Outbound alert. Broadcast on {@code /topic/alerts} and included in the dashboard
 * summary. Mirrors the frontend Alert type (ISO-8601 string timestamps).
 */
public record AlertDto(
        Long id,
        AlertType type,
        AlertSeverity severity,
        AlertStatus status,
        String message,
        String source,
        String productCode,
        String sensorKey,
        Long simulationRunId,
        String createdAt,
        String acknowledgedBy,
        String acknowledgedAt,
        String resolvedAt
) {

    public static AlertDto from(Alert alert) {
        return new AlertDto(
                alert.getId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getMessage(),
                alert.getSource(),
                alert.getProductCode(),
                alert.getSensorKey(),
                alert.getSimulationRunId(),
                toIso(alert.getCreatedAt()),
                alert.getAcknowledgedBy(),
                toIso(alert.getAcknowledgedAt()),
                toIso(alert.getResolvedAt())
        );
    }

    private static String toIso(java.time.Instant instant) {
        return instant != null ? instant.toString() : null;
    }
}
