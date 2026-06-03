package com.smartiot.qualityinspection.dashboard.dto;

import com.smartiot.qualityinspection.alert.dto.AlertDto;
import com.smartiot.qualityinspection.common.enums.SimulationState;
import com.smartiot.qualityinspection.inspection.dto.InspectionResultDto;
import com.smartiot.qualityinspection.sensor.dto.SensorHealthDto;

import java.util.List;

/**
 * Aggregated snapshot for the live dashboard. Returned by GET /api/dashboard/summary and
 * broadcast on {@code /topic/dashboard-summary}. Mirrors the frontend DashboardSummary type.
 */
public record DashboardSummaryDto(
        long passCount,
        long warningCount,
        long failCount,
        long totalInspected,
        long activeAlertCount,
        SimulationState simulationState,
        List<InspectionResultDto> latestResults,
        List<AlertDto> activeAlerts,
        List<SensorHealthDto> sensors
) {
}
