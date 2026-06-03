# Test Plan

How the implemented verification maps to the assignment's TC-E2E test cases. Automated
tests live in `backend/src/test/java`; run them with `./mvnw test` (25 tests).

## Automated tests

| Test class | Cases | What it proves |
|---|---|---|
| `QualityInspectionEngineTest` | TC-E2E-01, 02, 11 | PASS / WARNING / FAIL classification; explanation lists the most severe reason first (FR-10, FR-11) |
| `ReadingValidatorTest` | TC-E2E-09 | Valid packets accepted; missing fields / wrong type / bad timestamp rejected (FR-08, NFR-04) |
| `SimulationServiceTest` | TC-E2E-06 | start/pause/stop/reset transitions; invalid transitions rejected (FR-19) |
| `ThresholdServiceTest` | TC-E2E-05 | Threshold update saved; min ≤ warnMin ≤ warnMax ≤ max enforced (FR-18) |
| `AccessControlIntegrationTest` | TC-E2E-10, 16, 17 | Unauthenticated → 401; wrong role → 403; admin allowed; malformed JSON → 400 (NFR-11) |

## Verified manually / via live harness during development

| Area | Cases | Evidence |
|---|---|---|
| End-to-end happy path (sensor → ingest → classify → dashboard) | TC-E2E-01 | STOMP client received readings + results; dashboard rendered PASS counts |
| Defect / FAIL + alert | TC-E2E-02 | Overweight & crack faults produced FAIL + FAILED_PRODUCT alert |
| Machine health / maintenance alert | TC-E2E-03 | Temperature/vibration over limit produced MAINTENANCE alert |
| Sensor resilience & recovery | TC-E2E-04 | SENSOR_DISCONNECT → offline + SENSOR_HEALTH alert → reconnect → online |
| Dynamic threshold update (DM-01) | TC-E2E-05 | Admin PUT threshold applied to later classifications without restart |
| Simulation state across clients (DM-02) | TC-E2E-06 | Start/pause/stop/reset broadcast on `/topic/simulation-state` |
| Traceability drill-down (DM-03) | TC-E2E-07 | `GET /api/products/{id}` returns readings + result + alerts |
| Historical reporting & filtering (DM-04) | TC-E2E-08 | `GET /api/reports/quality-summary` with date filter; Recharts distribution |
| Real-time latency / load | TC-E2E-12, 13, 14 | Readings/results broadcast within the tick under multi-sensor load |
| Access control & fault injection security | TC-E2E-10, 16 | Operator/manager blocked (403) from admin actions; admin allowed |
| Input validation / malformed data | TC-E2E-09, 17, 18 | Validator rejects bad packets; malformed JSON → 400; safe handling |

## Notes

- Tests use an isolated in-memory H2 database (`src/test/resources/application.properties`),
  so they never touch the demo file database.
- The seeded users and thresholds make every run repeatable with predictable PASS/WARNING/FAIL
  inputs (NFR-12).
