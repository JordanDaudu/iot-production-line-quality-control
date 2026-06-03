package com.smartiot.qualityinspection.websocket.service;

import com.smartiot.qualityinspection.sensor.dto.SensorReadingDto;
import com.smartiot.qualityinspection.simulation.dto.SimulationStatusDto;
import com.smartiot.qualityinspection.websocket.WebSocketTopics;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Single point that pushes real-time updates to subscribed dashboard clients over STOMP.
 * Keeping all broadcasts here ensures topic names and payloads stay consistent.
 */
@Service
public class RealtimeBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public RealtimeBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastReading(SensorReadingDto reading) {
        messagingTemplate.convertAndSend(WebSocketTopics.READINGS, reading);
    }

    public void broadcastSimulationState(SimulationStatusDto status) {
        messagingTemplate.convertAndSend(WebSocketTopics.SIMULATION_STATE, status);
    }
}
