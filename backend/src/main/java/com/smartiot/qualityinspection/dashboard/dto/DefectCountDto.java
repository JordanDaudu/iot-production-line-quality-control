package com.smartiot.qualityinspection.dashboard.dto;

/**
 * Count of a single visual defect category for the defect Pareto chart.
 */
public record DefectCountDto(String category, long count) {
}
