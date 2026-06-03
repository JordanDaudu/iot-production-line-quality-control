package com.smartiot.qualityinspection.websocket.handler;

import com.smartiot.qualityinspection.common.exception.ValidationException;
import com.smartiot.qualityinspection.sensor.dto.IngestAck;
import com.smartiot.qualityinspection.sensor.dto.SensorHeartbeatMessage;
import com.smartiot.qualityinspection.sensor.dto.SensorReadingMessage;
import com.smartiot.qualityinspection.sensor.service.IngestionService;
import com.smartiot.qualityinspection.sensor.service.SensorHealthService;
import com.smartiot.qualityinspection.websocket.WebSocketTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Inbound STOMP endpoints for external sensor clients (FR-07, FR-08, FR-17). Readings and
 * heartbeats submitted here flow through the SAME validated ingestion path the in-process
 * simulators use; invalid packets are rejected safely and the sender is told why via
 * {@code /topic/ingest-ack}.
 */
@Controller
public class InboundSensorController {

    private static final Logger log = LoggerFactory.getLogger(InboundSensorController.class);

    private final IngestionService ingestionService;
    private final SensorHealthService sensorHealthService;
    private final SimpMessagingTemplate messagingTemplate;

    public InboundSensorController(IngestionService ingestionService,
                                   SensorHealthService sensorHealthService,
                                   SimpMessagingTemplate messagingTemplate) {
        this.ingestionService = ingestionService;
        this.sensorHealthService = sensorHealthService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sensor-reading")
    public void onSensorReading(SensorReadingMessage message) {
        String sensorKey = message != null ? message.sensorKey() : null;
        try {
            ingestionService.ingest(message);
            ack(sensorKey, true, null);
        } catch (ValidationException ex) {
            log.warn("Rejected inbound reading from {}: {}", sensorKey, ex.getMessage());
            ack(sensorKey, false, ex.getMessage());
        } catch (Exception ex) {
            log.warn("Failed to process inbound reading from {}", sensorKey, ex);
            ack(sensorKey, false, "Reading could not be processed.");
        }
    }

    @MessageMapping("/sensor-heartbeat")
    public void onHeartbeat(SensorHeartbeatMessage heartbeat) {
        if (heartbeat == null || heartbeat.sensorKey() == null) {
            return;
        }
        sensorHealthService.heartbeat(heartbeat.sensorKey(), heartbeat.sensorType(), heartbeat.timestamp());
    }

    private void ack(String sensorKey, boolean accepted, String error) {
        messagingTemplate.convertAndSend(WebSocketTopics.INGEST_ACK, new IngestAck(sensorKey, accepted, error));
    }
}
