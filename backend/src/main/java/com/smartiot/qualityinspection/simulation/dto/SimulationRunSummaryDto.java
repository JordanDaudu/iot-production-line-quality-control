package com.smartiot.qualityinspection.simulation.dto;

import com.smartiot.qualityinspection.common.enums.SimulationState;

/**
 * Lightweight summary of a persisted simulation run. Used by GET /api/simulation/runs so
 * the frontend can show existing runs and pre-check a new run name client-side (the
 * authoritative uniqueness check still happens server-side on start).
 *
 * @param id        run id
 * @param name      user-supplied run name (may be null for legacy runs)
 * @param scenario  scenario name
 * @param state     current/last state
 * @param startedAt ISO-8601 start timestamp
 */
public record SimulationRunSummaryDto(
        Long id,
        String name,
        String scenario,
        SimulationState state,
        String startedAt
) {
}
