// Mirrors the backend SimulationState enum.
export type SimulationState = 'IDLE' | 'RUNNING' | 'PAUSED' | 'STOPPED';

export interface SimulationStatus {
  simulationRunId: number | null;
  name: string | null;
  scenario: string | null;
  state: SimulationState;
}

// Mirrors the backend SimulationRunSummaryDto (GET /api/simulation/runs).
export interface SimulationRunSummary {
  id: number;
  name: string | null;
  scenario: string;
  state: SimulationState;
  startedAt: string;
}

// Mirrors the backend FaultType enum.
export type FaultType =
  | 'OVERWEIGHT_PRODUCT'
  | 'VISUAL_DEFECT'
  | 'TEMPERATURE_SPIKE'
  | 'VIBRATION_SPIKE'
  | 'SENSOR_DISCONNECT';
