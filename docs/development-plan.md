# Development Plan

The project is delivered in runnable increments. Each increment compiles, runs, and adds
a coherent slice of behaviour. Steps map to the assignment's MVP order.

## Increment 1 — Foundation ✅ (current)
1. Backend Spring Boot project + frontend React/Vite project.
2. CORS + WebSocket/STOMP configured.
3. Database entities + repositories.
- Plus: Spring Security with seeded users, login screen, dashboard with live WebSocket
  connection status.

## Increment 2 — Simulation + sensors ✅
4. Simulation lifecycle: start / pause / stop / reset (SimulationService + REST, role-guarded).
5. Virtual sensor generator service — in-process simulators on a scheduled tick.
6. Sensor reading validation + persistence through the validated inbound channel
   (IngestionService → ReadingValidator).
- Live data streams on `/topic/readings`; state on `/topic/simulation-state`.
- Frontend: shared STOMP connection (StompProvider), Simulation control page, dashboard
  live readings feed.

## Increment 3 — Quality + alerts + live dashboard
7. Quality inspection engine (PASS / WARNING / FAIL) + explanations.
8. Alert generation (failed product, maintenance, sensor health).
9. Live dashboard WebSocket updates (KPIs, latest products, alerts, sensor health).

## Increment 4 — Details, config, reports, faults
10. Product details + traceability.
11. Threshold configuration (admin-only).
12. Historical reports + filtering.
13. Fault injection (admin-only).

## Increment 5 — Polish + evidence
14. Demo seed data, README finalisation, test cases mapped to TC-E2E-01..19.

## Key design decisions
- **In-process sensors publish through the same validated inbound channel** an external
  client would use, so validation/abuse tests remain meaningful.
- **ID-reference links** between entities (not cross-module JPA relationships) keep modules
  decoupled and mirror the message/DTO payloads; traceability is via indexed queries.
- **Quality logic lives only in the `inspection` module** — never in controllers or the
  frontend.
- **Thresholds are hot-reloaded** from the database; no restart needed.
- **Spring Security enforces role checks server-side** (`@PreAuthorize`), not just in the UI.
