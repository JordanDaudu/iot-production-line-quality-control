# WebSocket / STOMP Events

Handshake endpoint: **`/ws`** (SockJS enabled). Application destination prefix: `/app`.
Broadcast (topic) prefix: `/topic`. Names are mirrored in
`frontend/src/websocket/eventTypes.ts`.

## Inbound (client → backend, `@MessageMapping`)

| Destination | Purpose | Payload (planned) |
|---|---|---|
| `/app/sensor-reading` | A virtual sensor submits a reading | `SensorReadingMessage` |
| `/app/sensor-heartbeat` | A sensor reports it is alive | `SensorHeartbeatMessage` |
| `/app/simulation-command` | Start/pause/stop/reset command | `SimulationCommandMessage` |

> In-process simulators publish through the **same validated ingestion path** an external
> client would hit, so validation applies uniformly.

## Outbound (backend → clients, `/topic`)

| Topic | Emitted when | Payload |
|---|---|---|
| `/topic/readings` | A reading is validated and stored | `SensorReading` |
| `/topic/inspection-results` | A product is classified | `InspectionResult` |
| `/topic/alerts` | An alert is created/updated | `Alert` |
| `/topic/sensor-health` | A sensor goes online/offline | `SensorHealth` |
| `/topic/simulation-state` | Simulation state changes | `SimulationStatus` |
| `/topic/dashboard-summary` | Aggregated dashboard snapshot | `DashboardSummary` |

Message payload schemas (valid + invalid examples) are documented in `api-schema.md`
as they are implemented (FR-26).
