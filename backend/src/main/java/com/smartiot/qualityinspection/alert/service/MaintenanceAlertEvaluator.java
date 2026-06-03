package com.smartiot.qualityinspection.alert.service;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.event.SensorReadingIngestedEvent;
import com.smartiot.qualityinspection.sensor.model.SensorReading;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import com.smartiot.qualityinspection.threshold.repository.ThresholdConfigurationRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

/**
 * Listens to ingested readings and raises a maintenance alert when a machine sensor
 * (temperature or vibration) exceeds its hard limit (FR-16). Only the FAIL band triggers
 * an alert, which keeps alert volume sensible for the demo.
 */
@Service
public class MaintenanceAlertEvaluator {

    private static final Set<SensorType> MACHINE_SENSORS =
            EnumSet.of(SensorType.TEMPERATURE, SensorType.VIBRATION);

    private final ThresholdConfigurationRepository thresholdRepository;
    private final AlertService alertService;

    public MaintenanceAlertEvaluator(ThresholdConfigurationRepository thresholdRepository,
                                     AlertService alertService) {
        this.thresholdRepository = thresholdRepository;
        this.alertService = alertService;
    }

    @EventListener
    public void onReadingIngested(SensorReadingIngestedEvent event) {
        SensorReading reading = event.reading();
        if (!MACHINE_SENSORS.contains(reading.getSensorType()) || reading.getValue() == null) {
            return;
        }
        thresholdRepository.findBySensorType(reading.getSensorType()).ifPresent(threshold -> {
            if (reading.getValue() > threshold.getMaxValue()) {
                alertService.createMaintenanceAlert(reading, threshold);
            }
        });
    }
}
