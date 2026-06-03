// Mirrors the backend SimulationState enum.
export type SimulationState = 'IDLE' | 'RUNNING' | 'PAUSED' | 'STOPPED';

export interface SimulationStatus {
  simulationRunId: number | null;
  scenario: string | null;
  state: SimulationState;
}

// Mirrors the backend FaultType enum.
export type FaultType =
  | 'OVERWEIGHT_PRODUCT'
  | 'VISUAL_DEFECT'
  | 'TEMPERATURE_SPIKE'
  | 'VIBRATION_SPIKE'
  | 'SENSOR_DISCONNECT';
