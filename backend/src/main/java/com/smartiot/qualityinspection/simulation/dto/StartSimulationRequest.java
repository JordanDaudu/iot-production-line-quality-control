package com.smartiot.qualityinspection.simulation.dto;

/**
 * Optional body for POST /api/simulation/start. When omitted, the default scenario is used.
 *
 * @param scenario scenario name to run (e.g. NORMAL_RUN)
 */
public record StartSimulationRequest(String scenario) {
}
