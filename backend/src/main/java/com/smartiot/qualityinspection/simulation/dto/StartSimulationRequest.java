package com.smartiot.qualityinspection.simulation.dto;

/**
 * Optional body for POST /api/simulation/start. When omitted, the default scenario is used.
 * A {@code name} is required only when starting a brand-new run; it is ignored when
 * resuming a paused run.
 *
 * @param name     user-supplied name for the run (required for a new run, unique case-insensitively)
 * @param scenario scenario name to run (e.g. NORMAL_RUN)
 */
public record StartSimulationRequest(String name, String scenario) {
}
