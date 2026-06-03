package com.smartiot.qualityinspection.inspection.dto;

import com.smartiot.qualityinspection.common.enums.QualityStatus;
import com.smartiot.qualityinspection.inspection.model.InspectionResult;

/**
 * Outbound inspection result. Broadcast on {@code /topic/inspection-results} and included
 * in the dashboard summary. Mirrors the frontend InspectionResult type (ISO-8601 string
 * timestamp).
 */
public record InspectionResultDto(
        Long id,
        String productCode,
        Long batchId,
        Long simulationRunId,
        QualityStatus status,
        Double score,
        String explanation,
        String createdAt
) {

    public static InspectionResultDto from(InspectionResult result) {
        return new InspectionResultDto(
                result.getId(),
                result.getProductCode(),
                result.getBatchId(),
                result.getSimulationRunId(),
                result.getStatus(),
                result.getScore(),
                result.getExplanation(),
                result.getCreatedAt() != null ? result.getCreatedAt().toString() : null
        );
    }
}
