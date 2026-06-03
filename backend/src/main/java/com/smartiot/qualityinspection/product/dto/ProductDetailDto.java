package com.smartiot.qualityinspection.product.dto;

import com.smartiot.qualityinspection.alert.dto.AlertDto;
import com.smartiot.qualityinspection.inspection.dto.InspectionResultDto;
import com.smartiot.qualityinspection.sensor.dto.SensorReadingDto;

import java.util.List;

/**
 * Full traceability view for a single product: its identity, the simulation run/batch it
 * belongs to, every reading in chronological order, the final inspection result and any
 * related alerts (FR-13, FR-22).
 */
public record ProductDetailDto(
        String productCode,
        Long batchId,
        String batchCode,
        Long simulationRunId,
        String scenario,
        String createdAt,
        InspectionResultDto result,
        List<SensorReadingDto> readings,
        List<AlertDto> alerts
) {
}
