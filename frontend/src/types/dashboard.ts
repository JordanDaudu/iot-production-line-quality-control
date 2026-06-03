import type { InspectionResult } from './inspection';
import type { Alert } from './alert';
import type { SensorHealth } from './sensor';
import type { SimulationState } from './simulation';

// Mirrors the backend dashboard summary DTO (built in a later increment).
export interface DashboardSummary {
  passCount: number;
  warningCount: number;
  failCount: number;
  totalInspected: number;
  activeAlertCount: number;
  simulationState: SimulationState;
  latestResults: InspectionResult[];
  activeAlerts: Alert[];
  sensors: SensorHealth[];
}
