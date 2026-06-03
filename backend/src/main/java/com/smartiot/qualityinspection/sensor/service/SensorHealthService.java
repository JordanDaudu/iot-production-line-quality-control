package com.smartiot.qualityinspection.sensor.service;

import com.smartiot.qualityinspection.common.event.SensorReadingIngestedEvent;
import com.smartiot.qualityinspection.common.event.SensorRecoveredEvent;
import com.smartiot.qualityinspection.common.event.SensorWentOfflineEvent;
import com.smartiot.qualityinspection.sensor.dto.SensorHealthDto;
import com.smartiot.qualityinspection.sensor.model.Sensor;
import com.smartiot.qualityinspection.sensor.model.SensorReading;
import com.smartiot.qualityinspection.sensor.repository.SensorRepository;
import com.smartiot.qualityinspection.simulation.service.SimulationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Tracks virtual sensor health. Each ingested reading marks its sensor online and records
 * the last-seen time. A scheduled monitor marks a sensor offline when it has not produced a
 * reading within the configured window (FR-17) — exercised by the sensor-disconnect fault.
 * Offline/recovery transitions are published as events so the alert module can react
 * without this service depending on it.
 */
@Service
public class SensorHealthService {

    private static final Logger log = LoggerFactory.getLogger(SensorHealthService.class);

    private final SensorRepository sensorRepository;
    private final SimulationService simulationService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${sensor.offline-threshold-ms:10000}")
    private long offlineThresholdMs;

    public SensorHealthService(SensorRepository sensorRepository,
                               SimulationService simulationService,
                               ApplicationEventPublisher eventPublisher) {
        this.sensorRepository = sensorRepository;
        this.simulationService = simulationService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void onReadingIngested(SensorReadingIngestedEvent event) {
        SensorReading reading = event.reading();
        Sensor sensor = sensorRepository.findBySensorKey(reading.getSensorKey())
                .orElseGet(() -> new Sensor(reading.getSensorKey(), reading.getSensorType(), true, null));
        boolean wasOffline = sensor.getId() != null && !sensor.isOnline();

        sensor.setOnline(true);
        sensor.setLastSeenAt(reading.getTimestamp());
        Sensor saved = sensorRepository.save(sensor);

        if (wasOffline) {
            log.info("Sensor {} recovered (online)", saved.getSensorKey());
            eventPublisher.publishEvent(new SensorRecoveredEvent(saved));
        }
    }

    @Scheduled(fixedDelayString = "${sensor.health-check-interval-ms:5000}")
    public void monitorHealth() {
        if (!simulationService.isRunning()) {
            return; // only meaningful while the line is running
        }
        Instant cutoff = Instant.now().minusMillis(offlineThresholdMs);
        for (Sensor sensor : sensorRepository.findAll()) {
            if (sensor.isOnline() && sensor.getLastSeenAt() != null && sensor.getLastSeenAt().isBefore(cutoff)) {
                sensor.setOnline(false);
                Sensor saved = sensorRepository.save(sensor);
                log.info("Sensor {} marked offline (no readings for >{}ms)", saved.getSensorKey(), offlineThresholdMs);
                eventPublisher.publishEvent(new SensorWentOfflineEvent(saved));
            }
        }
    }

    public List<SensorHealthDto> getSensorHealth() {
        return sensorRepository.findAll().stream()
                .sorted(Comparator.comparing(Sensor::getSensorKey))
                .map(SensorHealthDto::from)
                .toList();
    }
}
