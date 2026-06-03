package com.smartiot.qualityinspection.simulation.dto;

import com.smartiot.qualityinspection.common.enums.SimulationState;

/**
 * Current simulation status, returned by the REST API and broadcast on
 * {@code /topic/simulation-state}. Mirrors the frontend SimulationStatus type.
 *
 * @param simulationRunId id of the active run, or null when idle
 * @param scenario        active scenario name, or null when idle
 * @param state           current simulation state
 */
public record SimulationStatusDto(
        Long simulationRunId,
        String scenario,
        SimulationState state
) {
}
