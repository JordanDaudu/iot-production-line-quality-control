# REST API Schema

Base path: `/api`. Authentication: **HTTP Basic** (seeded users). Mutating
simulation / threshold / fault endpoints require specific roles (enforced server-side).

## Auth
| Method | Path | Description | Auth |
|---|---|---|---|
| GET | `/api/auth/me` | Current user + role | Any authenticated |

**`CurrentUserDto`**
```json
{ "username": "admin", "displayName": "System Administrator", "role": "ADMINISTRATOR" }
```

## Dashboard / products / alerts / reports / thresholds / simulation
The following endpoints are implemented across increments 2–4. Listed here so the
contract is fixed up front and the frontend types stay aligned.

| Method | Path | Description | Role |
|---|---|---|---|
| GET | `/api/dashboard/summary` | KPI snapshot | authenticated |
| GET | `/api/products` | Filter by `status,batchId,simulationRunId,from,to` | authenticated |
| GET | `/api/products/{productId}` | Product details + readings + result + alerts | authenticated |
| GET | `/api/alerts` | List alerts (filterable) | authenticated |
| POST | `/api/alerts/{id}/acknowledge` | Acknowledge an alert | MAINTENANCE_TECHNICIAN / ADMINISTRATOR |
| POST | `/api/alerts/{id}/resolve` | Resolve an alert | MAINTENANCE_TECHNICIAN / ADMINISTRATOR |
| GET | `/api/reports/quality-summary` | Aggregated stats | authenticated |
| GET | `/api/thresholds` | List threshold configs | authenticated |
| PUT | `/api/thresholds/{sensorType}` | Update thresholds | ADMINISTRATOR |
| GET | `/api/simulation/state` | Current simulation state | authenticated |
| POST | `/api/simulation/start` | Start simulation | OPERATOR / ADMINISTRATOR |
| POST | `/api/simulation/pause` | Pause simulation | OPERATOR / ADMINISTRATOR |
| POST | `/api/simulation/stop` | Stop simulation | OPERATOR / ADMINISTRATOR |
| POST | `/api/simulation/reset` | Reset live state | OPERATOR / ADMINISTRATOR |
| POST | `/api/simulation/faults` | Inject a fault | ADMINISTRATOR |
| GET | `/api/users` | List users + roles | ADMINISTRATOR |
| PUT | `/api/users/{id}/role` | Change a user's role | ADMINISTRATOR |

## Request schemas & examples (FR-26)

**Threshold update** — `PUT /api/thresholds/{sensorType}` (admin). Validated: `min ≤ warnMin ≤ warnMax ≤ max`.
```json
// valid
{ "minValue": 90, "warnMinValue": 95, "warnMaxValue": 105, "maxValue": 110, "unit": "g" }
// invalid -> 400 "Thresholds must satisfy min <= warnMin <= warnMax <= max."
{ "minValue": 200, "warnMinValue": 95, "warnMaxValue": 105, "maxValue": 110, "unit": "g" }
```

**Start simulation** — `POST /api/simulation/start` (operator/admin). Scenario is one of
`NORMAL_RUN`, `HIGH_DEFECT_RATE`, `TEMPERATURE_SPIKE`, `VIBRATION_FAULT`, `SENSOR_DISCONNECT`,
`MIXED_FAULT_DEMO` (FR-25):
```json
{ "scenario": "HIGH_DEFECT_RATE" }
```

**Fault injection** — `POST /api/simulation/faults` (admin):
```json
{ "faultType": "SENSOR_DISCONNECT", "sensorKey": "VIBRATION-1", "durationSeconds": 20 }
```
`faultType` ∈ `OVERWEIGHT_PRODUCT, VISUAL_DEFECT, TEMPERATURE_SPIKE, VIBRATION_SPIKE, SENSOR_DISCONNECT`.

**Acknowledge alert** — `POST /api/alerts/{id}/acknowledge` (tech/admin), optional note (FR-24):
```json
{ "note": "Checked station, replaced filter." }
```

**Product filters** — `GET /api/products?status=&batchId=&simulationRunId=&from=&to=&sensorType=`
(ISO-8601 dates; `sensorType` keeps products that have a reading of that sensor).
**Change role** — `PUT /api/users/{id}/role` (admin): `{ "role": "OPERATOR" }` (the last admin cannot be demoted).
**Report filters** — `GET /api/reports/quality-summary?batchId=&simulationRunId=&from=&to=` →
totals, rates, and a per-run breakdown.

WebSocket message schemas (SensorReading, Heartbeat, Alert, InspectionResult, IngestAck,
SimulationStatus) with valid/invalid examples are in `websocket-events.md`.

## Error format
All errors return a consistent body and never leak stack traces:
```json
{ "timestamp": "2026-06-03T10:15:30Z", "status": 400, "error": "Bad Request",
  "message": "weight: must not be null", "path": "/api/..." }
```
