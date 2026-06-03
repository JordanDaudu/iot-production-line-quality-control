package com.smartiot.qualityinspection.report.dto;

import java.util.List;

/**
 * Aggregated PASS/WARNING/FAIL statistics for the reports screen (FR-21), including a
 * per-simulation-run breakdown. Rates are percentages rounded to one decimal place.
 */
public record QualitySummaryReportDto(
        long total,
        long passCount,
        long warningCount,
        long failCount,
        double passRate,
        double warningRate,
        double failRate,
        List<RunBreakdown> byRun
) {

    /** Counts for a single simulation run. */
    public record RunBreakdown(
            Long simulationRunId,
            long total,
            long passCount,
            long warningCount,
            long failCount
    ) {
    }
}
