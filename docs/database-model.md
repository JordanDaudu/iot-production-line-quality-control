# Database Model

H2 file database (`./data/inspection.mv.db`), schema managed by Hibernate
(`ddl-auto=update`). Entities are linked by **ID references** (not JPA relationships),
which keeps modules decoupled and mirrors the message payloads. Traceability is achieved
through indexed columns.

## Entities

### simulation_run
`id, scenario, state {IDLE,RUNNING,PAUSED,STOPPED}, startedAt, stoppedAt`

### batch
`id, code (unique), simulationRunId, createdAt`

### product
`id, productCode (unique, e.g. BATCH001-P0001), batchId, simulationRunId, createdAt`

### sensor
`id, sensorKey (unique, e.g. WEIGHT-1), sensorType, online, lastSeenAt`

### sensor_reading  *(indexed: timestamp, sensorType, productCode, batchId, simulationRunId)*
`id, sensorType, sensorKey, productCode?, machineId?, batchId?, simulationRunId?,
 value?, unit?, defectCategory?, confidence?, timestamp`

### inspection_result  *(indexed: status, batchId, simulationRunId, createdAt)*
`id, productCode (unique), batchId, simulationRunId, status {PASS,WARNING,FAIL},
 score?, explanation, createdAt`

### alert  *(indexed: status, type, createdAt)*
`id, type {FAILED_PRODUCT,MAINTENANCE,SENSOR_HEALTH}, severity {INFO,WARNING,CRITICAL},
 status {ACTIVE,ACKNOWLEDGED,RESOLVED,CLEARED}, message, source?, productCode?,
 sensorKey?, simulationRunId?, createdAt, acknowledgedBy?, acknowledgedAt?, resolvedAt?`

### threshold_configuration
`id, sensorType (unique), minValue, warnMinValue, warnMaxValue, maxValue, unit,
 updatedAt, updatedByRole?`

Bands: `< min` FAIL · `min..warnMin` WARNING · `warnMin..warnMax` PASS ·
`warnMax..max` WARNING · `> max` FAIL.

### user_account
`id, username (unique), password (BCrypt), role {QUALITY_MANAGER, OPERATOR,
 MAINTENANCE_TECHNICIAN, ADMINISTRATOR}, displayName`

## Traceability chain
`SimulationRun → Batch → Product → SensorReading → InspectionResult`, reconstructed via
`simulationRunId / batchId / productCode` lookups.
