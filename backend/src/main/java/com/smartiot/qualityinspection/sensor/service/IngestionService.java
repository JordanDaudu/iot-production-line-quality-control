package com.smartiot.qualityinspection.sensor.service;

import com.smartiot.qualityinspection.common.event.SensorReadingIngestedEvent;
import com.smartiot.qualityinspection.sensor.dto.SensorReadingDto;
import com.smartiot.qualityinspection.sensor.dto.SensorReadingMessage;
import com.smartiot.qualityinspection.sensor.model.SensorReading;
import com.smartiot.qualityinspection.sensor.repository.SensorReadingRepository;
import com.smartiot.qualityinspection.websocket.service.RealtimeBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * The single validated inbound channel for sensor data. Every reading — whether produced
 * by an in-process simulator or (in future) an external client — passes through here:
 * validate, persist, then broadcast. Invalid readings are rejected before storage.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final ReadingValidator validator;
    private final SensorReadingRepository readingRepository;
    private final RealtimeBroadcaster broadcaster;
    private final ApplicationEventPublisher eventPublisher;

    public IngestionService(ReadingValidator validator,
                            SensorReadingRepository readingRepository,
                            RealtimeBroadcaster broadcaster,
                            ApplicationEventPublisher eventPublisher) {
        this.validator = validator;
        this.readingRepository = readingRepository;
        this.broadcaster = broadcaster;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Validates, stores and broadcasts a sensor reading.
     *
     * @return the persisted reading
     * @throws com.smartiot.qualityinspection.common.exception.ValidationException if invalid
     */
    public SensorReading ingest(SensorReadingMessage message) {
        validator.validate(message);

        SensorReading reading = toEntity(message);
        SensorReading saved = readingRepository.save(reading);

        broadcaster.broadcastReading(SensorReadingDto.from(saved));
        eventPublisher.publishEvent(new SensorReadingIngestedEvent(saved));
        log.debug("Ingested {} reading {} (sensor {})",
                saved.getSensorType(), saved.getId(), saved.getSensorKey());
        return saved;
    }

    private SensorReading toEntity(SensorReadingMessage message) {
        SensorReading reading = new SensorReading();
        reading.setSensorType(message.sensorType());
        reading.setSensorKey(message.sensorKey());
        reading.setProductCode(message.productCode());
        reading.setMachineId(message.machineId());
        reading.setBatchId(message.batchId());
        reading.setSimulationRunId(message.simulationRunId());
        reading.setValue(message.value());
        reading.setUnit(message.unit());
        reading.setDefectCategory(message.defectCategory());
        reading.setConfidence(message.confidence());
        reading.setTimestamp(message.timestamp());
        return reading;
    }
}
