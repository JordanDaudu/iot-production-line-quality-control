package com.smartiot.qualityinspection.websocket;

/**
 * Central registry of STOMP broadcast destinations. These MUST stay identical to the
 * frontend registry in {@code frontend/src/websocket/eventTypes.ts}.
 */
public final class WebSocketTopics {

    public static final String READINGS = "/topic/readings";
    public static final String INSPECTION_RESULTS = "/topic/inspection-results";
    public static final String ALERTS = "/topic/alerts";
    public static final String SENSOR_HEALTH = "/topic/sensor-health";
    public static final String SIMULATION_STATE = "/topic/simulation-state";
    public static final String DASHBOARD_SUMMARY = "/topic/dashboard-summary";
    /** Per-reading acknowledgement returned to sensor clients that submit over WebSocket. */
    public static final String INGEST_ACK = "/topic/ingest-ack";

    private WebSocketTopics() {
    }
}
