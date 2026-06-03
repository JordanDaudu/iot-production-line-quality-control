// Central registry of STOMP destinations. These names must stay identical to the
// backend (see WebSocketConfig and the @MessageMapping / SimpMessagingTemplate usage).

export const STOMP_ENDPOINT = '/ws';

// Topics the backend broadcasts to (frontend subscribes).
export const Topics = {
  READINGS: '/topic/readings',
  INSPECTION_RESULTS: '/topic/inspection-results',
  ALERTS: '/topic/alerts',
  SENSOR_HEALTH: '/topic/sensor-health',
  SIMULATION_STATE: '/topic/simulation-state',
  DASHBOARD_SUMMARY: '/topic/dashboard-summary',
} as const;

// Destinations the client sends to (backend @MessageMapping handlers).
export const AppDestinations = {
  SENSOR_READING: '/app/sensor-reading',
  SENSOR_HEARTBEAT: '/app/sensor-heartbeat',
  SIMULATION_COMMAND: '/app/simulation-command',
} as const;
