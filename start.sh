#!/usr/bin/env bash
# Starts the Smart IoT backend (Spring Boot) and frontend (Vite) together.
# Press Ctrl+C to stop both.
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "Starting backend  -> http://localhost:8080"
( cd "$ROOT/backend" && ./mvnw spring-boot:run ) &
BACKEND_PID=$!

echo "Starting frontend -> http://localhost:5173"
( cd "$ROOT/frontend" && npm install && npm run dev ) &
FRONTEND_PID=$!

trap 'echo; echo "Stopping..."; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null' EXIT INT TERM

echo
echo "Open http://localhost:5173 and sign in (admin / admin123). Ctrl+C to stop."
wait
