package com.smartiot.qualityinspection.threshold.dto;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;

/**
 * A sensor's configurable quality limits. Mirrors the frontend Threshold type.
 */
public record ThresholdDto(
        SensorType sensorType,
        Double minValue,
        Double warnMinValue,
        Double warnMaxValue,
        Double maxValue,
        String unit,
        String updatedAt,
        String updatedByRole
) {

    public static ThresholdDto from(ThresholdConfiguration t) {
        return new ThresholdDto(
                t.getSensorType(),
                t.getMinValue(),
                t.getWarnMinValue(),
                t.getWarnMaxValue(),
                t.getMaxValue(),
                t.getUnit(),
                t.getUpdatedAt() != null ? t.getUpdatedAt().toString() : null,
                t.getUpdatedByRole()
        );
    }
}
