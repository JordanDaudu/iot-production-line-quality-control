package com.smartiot.qualityinspection.report.dto;

/**
 * Aggregated PASS/WARNING/FAIL statistics for the reports screen (FR-21). Rates are
 * percentages rounded to one decimal place.
 */
public record QualitySummaryReportDto(
        long total,
        long passCount,
        long warningCount,
        long failCount,
        double passRate,
        double warningRate,
        double failRate
) {
}
