package com.smartiot.qualityinspection.threshold.service;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.exception.ResourceNotFoundException;
import com.smartiot.qualityinspection.common.exception.ValidationException;
import com.smartiot.qualityinspection.threshold.dto.ThresholdDto;
import com.smartiot.qualityinspection.threshold.dto.ThresholdUpdateRequest;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import com.smartiot.qualityinspection.threshold.repository.ThresholdConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Reads and updates quality thresholds. Updates are validated and persisted; because the
 * inspection engine reads the active configuration from the database on every decision,
 * changes apply immediately without a restart (FR-18).
 */
@Service
public class ThresholdService {

    private static final Logger log = LoggerFactory.getLogger(ThresholdService.class);

    private final ThresholdConfigurationRepository repository;

    public ThresholdService(ThresholdConfigurationRepository repository) {
        this.repository = repository;
    }

    public List<ThresholdDto> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(ThresholdConfiguration::getSensorType))
                .map(ThresholdDto::from)
                .toList();
    }

    public ThresholdDto update(SensorType sensorType, ThresholdUpdateRequest request, String updatedByRole) {
        validateOrdering(request);

        ThresholdConfiguration config = repository.findBySensorType(sensorType)
                .orElseThrow(() -> new ResourceNotFoundException("No threshold configured for " + sensorType));

        config.setMinValue(request.minValue());
        config.setWarnMinValue(request.warnMinValue());
        config.setWarnMaxValue(request.warnMaxValue());
        config.setMaxValue(request.maxValue());
        config.setUnit(request.unit());
        config.setUpdatedAt(Instant.now());
        config.setUpdatedByRole(updatedByRole);

        ThresholdDto dto = ThresholdDto.from(repository.save(config));
        log.info("Threshold for {} updated by {}: [{} | {} - {} | {}]",
                sensorType, updatedByRole, request.minValue(), request.warnMinValue(),
                request.warnMaxValue(), request.maxValue());
        return dto;
    }

    private void validateOrdering(ThresholdUpdateRequest r) {
        if (!(r.minValue() <= r.warnMinValue()
                && r.warnMinValue() <= r.warnMaxValue()
                && r.warnMaxValue() <= r.maxValue())) {
            throw new ValidationException(
                    "Thresholds must satisfy min <= warnMin <= warnMax <= max.");
        }
    }
}
