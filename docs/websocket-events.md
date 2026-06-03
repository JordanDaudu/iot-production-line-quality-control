# WebSocket / STOMP Events

Handshake endpoint: **`/ws`** (SockJS enabled). Application destination prefix: `/app`.
Broadcast (topic) prefix: `/topic`. Names are mirrored in
`frontend/src/websocket/eventTypes.ts` and `backend/.../websocket/WebSocketTopics.java`.

## Inbound (client → backend, `@MessageMapping`)

Implemented in `InboundSensorController`. Readings flow through the same validated
ingestion path the in-process simulators use.

| Destination | Purpose | Payload |
|---|---|---|
| `/app/sensor-reading` | A sensor submits a reading | `SensorReadingMessage` |
| `/app/sensor-heartbeat` | A sensor reports it is alive | `SensorHeartbeatMessage` |

> Simulation control (start/pause/stop/reset) and fault injection are exposed over the
> secured REST API (`/api/simulation/*`) rather than STOMP, so role checks apply.

## Outbound (backend → clients, `/topic`)

| Topic | Emitted when | Payload |
|---|---|---|
| `/topic/readings` | A reading is validated and stored | `SensorReading` |
| `/topic/inspection-results` | A product is classified | `InspectionResult` |
| `/topic/alerts` | An alert is created/updated | `Alert` |
| `/topic/sensor-health` | (reserved) sensor health snapshot | `SensorHealth` |
| `/topic/simulation-state` | Simulation state changes | `SimulationStatus` |
| `/topic/dashboard-summary` | Aggregated dashboard snapshot | `DashboardSummary` |
| `/topic/ingest-ack` | A reading submitted over WS is accepted/rejected | `IngestAck` |

---

## Message schemas (FR-26)

### SensorReadingMessage (inbound)
Required: `sensorType`, `sensorKey`, `timestamp`; `productCode` for WEIGHT/CAMERA/BARCODE;
`machineId` for TEMPERATURE/VIBRATION/HEALTH; numeric `value` for WEIGHT/TEMPERATURE/VIBRATION;
`defectCategory` for CAMERA.

Valid (product weight):
```json
{ "sensorType": "WEIGHT", "sensorKey": "WEIGHT-1", "productCode": "BATCH001-P0001",
  "batchId": 1, "simulationRunId": 1, "value": 100.4, "unit": "g",
  "timestamp": "2026-06-03T10:15:30Z" }
```
Invalid (missing productCode → rejected, ack `accepted:false`):
```json
{ "sensorType": "WEIGHT", "sensorKey": "WEIGHT-1", "value": 100.4,
  "timestamp": "2026-06-03T10:15:30Z" }
```
Invalid (non-numeric value):
```json
{ "sensorType": "WEIGHT", "sensorKey": "WEIGHT-1", "productCode": "BATCH001-P0001",
  "value": "heavy", "timestamp": "2026-06-03T10:15:30Z" }
```

### SensorHeartbeatMessage (inbound)
```json
{ "sensorKey": "TEMPERATURE-1", "sensorType": "TEMPERATURE", "timestamp": "2026-06-03T10:15:30Z" }
```

### IngestAck (outbound, `/topic/ingest-ack`)
```json
{ "sensorKey": "WEIGHT-1", "accepted": false, "error": "productCode is required for sensor type WEIGHT." }
```

### SensorReading (outbound, `/topic/readings`)
```json
{ "id": 42, "sensorType": "CAMERA", "sensorKey": "CAMERA-1", "productCode": "BATCH001-P0001",
  "machineId": null, "batchId": 1, "simulationRunId": 1, "value": null, "unit": null,
  "defectCategory": "OK", "confidence": 98.0, "timestamp": "2026-06-03T10:15:30.123Z" }
```

### InspectionResult (outbound, `/topic/inspection-results`)
```json
{ "id": 7, "productCode": "BATCH001-P0001", "batchId": 1, "simulationRunId": 1,
  "status": "WARNING", "score": 82.5, "explanation": "Visual defect detected: SCRATCH; Weight 100.4 g is within limits.",
  "createdAt": "2026-06-03T10:15:31Z" }
```

### Alert (outbound, `/topic/alerts`)
```json
{ "id": 3, "type": "FAILED_PRODUCT", "severity": "CRITICAL", "status": "ACTIVE",
  "message": "Product BATCH001-P0003 failed inspection: ...", "source": "Quality Engine",
  "productCode": "BATCH001-P0003", "sensorKey": null, "simulationRunId": 1,
  "createdAt": "2026-06-03T10:15:32Z", "acknowledgedBy": null, "acknowledgedAt": null,
  "resolvedAt": null, "note": null }
```

### SimulationStatus (outbound, `/topic/simulation-state`)
```json
{ "simulationRunId": 1, "scenario": "HIGH_DEFECT_RATE", "state": "RUNNING" }
```
