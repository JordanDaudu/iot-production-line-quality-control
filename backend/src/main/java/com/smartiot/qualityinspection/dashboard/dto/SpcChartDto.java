package com.smartiot.qualityinspection.dashboard.dto;

import java.util.List;

/**
 * Statistical Process Control (SPC) data for product weight over the active run: each
 * sample, the control limits (mean ± 3σ), the centre line and the spec limits. Points
 * outside the control limits are flagged as out-of-control.
 */
public record SpcChartDto(
        List<SpcPoint> points,
        double centerLine,
        double ucl,
        double lcl,
        double specLow,
        double specHigh,
        String unit
) {

    public record SpcPoint(int index, String productCode, double value, boolean outOfControl) {
    }
}
