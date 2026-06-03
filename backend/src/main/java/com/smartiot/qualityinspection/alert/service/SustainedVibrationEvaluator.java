package com.smartiot.qualityinspection.alert.service;

import com.smartiot.qualityinspection.common.enums.SensorType;
import com.smartiot.qualityinspection.common.event.SensorReadingIngestedEvent;
import com.smartiot.qualityinspection.sensor.model.SensorReading;
import com.smartiot.qualityinspection.threshold.model.ThresholdConfiguration;
import com.smartiot.qualityinspection.threshold.repository.ThresholdConfigurationRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Detects sustained abnormal vibration — distinct from a single extreme spike (FR-04).
 * Keeps a small rolling window of recent vibration values; if most of them stay above the
 * warning level (without necessarily exceeding the hard limit each time), it raises a
 * maintenance alert (FR-16). A cooldown prevents repeated alerts for the same episode.
 */
@Service
public class SustainedVibrationEvaluator {

    private static final int WINDOW = 5;
    private static final int REQUIRED_ELEVATED = 3;
    private static final Duration COOLDOWN = Duration.ofSeconds(30);

    private final ThresholdConfigurationRepository thresholdRepository;
    private final AlertService alertService;

    private final Deque<Double> recent = new ArrayDeque<>();
    private Instant lastAlertAt;

    public SustainedVibrationEvaluator(ThresholdConfigurationRepository thresholdRepository,
                                       AlertService alertService) {
        this.thresholdRepository = thresholdRepository;
        this.alertService = alertService;
    }

    @EventListener
    public synchronized void onReadingIngested(SensorReadingIngestedEvent event) {
        SensorReading reading = event.reading();
        if (reading.getSensorType() != SensorType.VIBRATION || reading.getValue() == null) {
            return;
        }
        ThresholdConfiguration threshold = thresholdRepository.findBySensorType(SensorType.VIBRATION).orElse(null);
        if (threshold == null) {
            return;
        }

        recent.addLast(reading.getValue());
        if (recent.size() > WINDOW) {
            recent.removeFirst();
        }
        if (recent.size() < WINDOW) {
            return;
        }

        long elevated = recent.stream().filter(v -> v > threshold.getWarnMaxValue()).count();
        if (elevated >= REQUIRED_ELEVATED && cooldownElapsed(reading.getTimestamp())) {
            lastAlertAt = reading.getTimestamp() != null ? reading.getTimestamp() : Instant.now();
            alertService.createSustainedVibrationAlert(reading);
        }
    }

    private boolean cooldownElapsed(Instant now) {
        if (lastAlertAt == null) {
            return true;
        }
        Instant reference = now != null ? now : Instant.now();
        return Duration.between(lastAlertAt, reference).compareTo(COOLDOWN) >= 0;
    }
}
