import type { InspectionResult } from './inspection';
import type { Alert } from './alert';
import type { SensorHealth } from './sensor';
import type { SimulationState } from './simulation';

// Mirrors the backend TelemetryPointDto.
export interface TelemetryPoint {
  timestamp: string;
  temperature: number | null;
  vibration: number | null;
}

// Mirrors the backend SpcChartDto.
export interface SpcPoint {
  index: number;
  productCode: string;
  value: number;
  outOfControl: boolean;
}

export interface SpcChart {
  points: SpcPoint[];
  centerLine: number;
  ucl: number;
  lcl: number;
  specLow: number;
  specHigh: number;
  unit: string;
}

// Mirrors the backend DefectCountDto.
export interface DefectCount {
  category: string;
  count: number;
}

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
