package com.smartiot.qualityinspection.simulation.service;

import com.smartiot.qualityinspection.common.enums.FaultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Holds pending injected faults that the simulator applies on its next tick (FR-20). Spike
 * and defect faults are one-shot booleans consumed by the simulator; a sensor disconnect
 * suppresses a sensor's readings for a duration, which the health monitor then detects as
 * offline.
 */
@Service
public class FaultInjectionService {

    private static final Logger log = LoggerFactory.getLogger(FaultInjectionService.class);

    private static final String DEFAULT_DISCONNECT_TARGET = "VIBRATION-1";
    private static final int DEFAULT_DISCONNECT_SECONDS = 20;

    private final AtomicBoolean overweight = new AtomicBoolean(false);
    private final AtomicBoolean visualDefect = new AtomicBoolean(false);
    private final AtomicBoolean temperatureSpike = new AtomicBoolean(false);
    private final AtomicBoolean vibrationSpike = new AtomicBoolean(false);
    private final Map<String, Instant> disconnectedUntil = new ConcurrentHashMap<>();

    public String inject(FaultType faultType, String sensorKey, Integer durationSeconds) {
        switch (faultType) {
            case OVERWEIGHT_PRODUCT -> overweight.set(true);
            case VISUAL_DEFECT -> visualDefect.set(true);
            case TEMPERATURE_SPIKE -> temperatureSpike.set(true);
            case VIBRATION_SPIKE -> vibrationSpike.set(true);
            case SENSOR_DISCONNECT -> {
                String target = (sensorKey != null && !sensorKey.isBlank()) ? sensorKey : DEFAULT_DISCONNECT_TARGET;
                int seconds = durationSeconds != null && durationSeconds > 0 ? durationSeconds : DEFAULT_DISCONNECT_SECONDS;
                disconnectedUntil.put(target, Instant.now().plusSeconds(seconds));
                log.info("Injected SENSOR_DISCONNECT for {} ({}s)", target, seconds);
                return "Sensor " + target + " disconnected for " + seconds + "s";
            }
        }
        log.info("Injected fault {}", faultType);
        return faultType + " will be applied on the next production cycle";
    }

    public boolean consumeOverweight() {
        return overweight.getAndSet(false);
    }

    public boolean consumeVisualDefect() {
        return visualDefect.getAndSet(false);
    }

    public boolean consumeTemperatureSpike() {
        return temperatureSpike.getAndSet(false);
    }

    public boolean consumeVibrationSpike() {
        return vibrationSpike.getAndSet(false);
    }

    public boolean isDisconnected(String sensorKey) {
        Instant until = disconnectedUntil.get(sensorKey);
        if (until == null) {
            return false;
        }
        if (Instant.now().isAfter(until)) {
            disconnectedUntil.remove(sensorKey);
            return false;
        }
        return true;
    }

    /** Clears all pending faults (used on simulation reset). */
    public void reset() {
        overweight.set(false);
        visualDefect.set(false);
        temperatureSpike.set(false);
        vibrationSpike.set(false);
        disconnectedUntil.clear();
    }
}
