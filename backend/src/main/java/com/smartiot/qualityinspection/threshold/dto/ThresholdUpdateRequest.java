package com.smartiot.qualityinspection.threshold.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body to update a sensor's thresholds. Field presence is validated by Bean
 * Validation; the min &le; warnMin &le; warnMax &le; max ordering is checked in the service.
 */
public record ThresholdUpdateRequest(
        @NotNull Double minValue,
        @NotNull Double warnMinValue,
        @NotNull Double warnMaxValue,
        @NotNull Double maxValue,
        @NotBlank String unit
) {
}
