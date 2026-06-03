# Smart IoT Quality Inspection System

A real-time **Industry 4.0 production-line simulation**. Virtual IoT sensors generate
readings, the backend validates and stores them, a rule-based engine classifies each
product as **PASS / WARNING / FAIL**, and a live dashboard updates over WebSockets with
alerts, traceability and historical reports. No physical hardware required.

> Monorepo: **Spring Boot** backend + **React/TypeScript (Vite)** frontend.

---

## Features

- **Virtual sensors** (weight, temperature, vibration, camera, barcode) streaming on a tick.
- **Validated ingestion** — a single chokepoint validates every reading before storage.
- **Quality engine** — rule-based PASS/WARNING/FAIL with human-readable explanations.
- **Alerts** — failed-product, maintenance, and sensor-health (offline detection + recovery),
  with acknowledge/resolve lifecycle.
- **Live dashboard** — KPIs, latest results, active alerts, sensor status, raw reading feed.
- **Traceability** — drill down from any product to its readings, result and alerts.
- **Admin tools** — threshold configuration (hot-applied), fault injection, simulation control.
- **Reports** — historical PASS/WARNING/FAIL distribution with a date filter (Recharts).
- **Role-based access** — enforced server-side with Spring Security.

## Tech stack

| | |
|---|---|
| Backend | Java 21, Spring Boot 3.4, Spring Web, WebSocket/STOMP, Data JPA, Bean Validation, Spring Security |
| Database | H2 file (default) → PostgreSQL optional |
| Build | Maven (wrapper committed) |
| Frontend | React + TypeScript, Vite, React Router, Axios, @stomp/stompjs + SockJS, Recharts |

## Prerequisites

- **JDK 21** (LTS) and **Node.js 20+**

## Running locally

**Quick start (one command):**

- **Windows:** double-click **`start.cmd`** (or run it from a terminal). It auto-selects a
  Java 21/23 JDK for the backend and opens the backend and frontend each in their own window.
- **macOS / Linux / Replit:** `./start.sh`

Then open http://localhost:5173 and sign in with `admin` / `admin123`.

**Or start each side manually:**

```bash
# Backend (port 8080)
cd backend
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run

# Frontend (port 5173, proxies /api and /ws to the backend)
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**, sign in, go to **Simulation → Start**, and watch the
**Dashboard** classify products live.

## Demo accounts

Seeded automatically on first run (password = username + `123`):

| Username | Role | Can |
|---|---|---|
| `admin` | System Administrator | everything (thresholds, faults, scenarios) |
| `manager` | Quality Manager | view dashboards, products, reports |
| `operator` | Production Line Operator | control the simulation |
| `tech` | Maintenance Technician | acknowledge/resolve alerts |

## Testing

```bash
cd backend
./mvnw test
```
25 tests cover the quality engine, validation, the simulation state machine, threshold
rules and role-based access control. See [`docs/test-plan.md`](docs/test-plan.md) for the
mapping to the TC-E2E test cases.

## API & WebSocket reference

- REST endpoints: [`docs/api-schema.md`](docs/api-schema.md)
- STOMP topics/events: [`docs/websocket-events.md`](docs/websocket-events.md)
- Database model: [`docs/database-model.md`](docs/database-model.md)
- Build plan / increments: [`docs/development-plan.md`](docs/development-plan.md)

Quick reference:

- App: http://localhost:5173 · API base: http://localhost:8080/api
- H2 console: http://localhost:8080/h2-console (JDBC `jdbc:h2:file:./data/inspection`)
- WebSocket (STOMP/SockJS): `/ws` → topics `/topic/readings`, `/topic/inspection-results`,
  `/topic/alerts`, `/topic/sensor-health`, `/topic/simulation-state`, `/topic/dashboard-summary`

## Project structure

```
backend/    Spring Boot app — modular packages: sensor, simulation, inspection, alert,
            dashboard, report, threshold, product, websocket, config, common, auth
frontend/   React + Vite app — api, websocket, types, pages, components, hooks, context
docs/       API schema, WebSocket events, database model, development plan, test plan
```

## Status

All MVP steps complete (Increments 1–5): foundation, simulation + sensors, quality engine
+ alerts + live dashboard, product details + thresholds + reports + fault injection, and
automated tests.
