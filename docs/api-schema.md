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

## Error format
All errors return a consistent body and never leak stack traces:
```json
{ "timestamp": "2026-06-03T10:15:30Z", "status": 400, "error": "Bad Request",
  "message": "weight: must not be null", "path": "/api/..." }
```
