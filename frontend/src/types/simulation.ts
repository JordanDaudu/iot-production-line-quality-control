// Mirrors the backend SimulationState enum.
export type SimulationState = 'IDLE' | 'RUNNING' | 'PAUSED' | 'STOPPED';

export interface SimulationStatus {
  simulationRunId: number | null;
  scenario: string | null;
  state: SimulationState;
}
