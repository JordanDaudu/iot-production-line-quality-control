// Mirrors the backend Alert entity and its enums.
export type AlertType = 'FAILED_PRODUCT' | 'MAINTENANCE' | 'SENSOR_HEALTH';
export type AlertSeverity = 'INFO' | 'WARNING' | 'CRITICAL';
export type AlertStatus = 'ACTIVE' | 'ACKNOWLEDGED' | 'RESOLVED' | 'CLEARED';

export interface Alert {
  id: number;
  type: AlertType;
  severity: AlertSeverity;
  status: AlertStatus;
  message: string;
  source?: string | null;
  productCode?: string | null;
  sensorKey?: string | null;
  simulationRunId?: number | null;
  createdAt: string;
  acknowledgedBy?: string | null;
  acknowledgedAt?: string | null;
  resolvedAt?: string | null;
  note?: string | null;
}
