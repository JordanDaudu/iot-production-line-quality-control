// Mirrors the backend SensorType enum, SensorReading and Sensor entities.
export type SensorType =
  | 'WEIGHT'
  | 'TEMPERATURE'
  | 'VIBRATION'
  | 'CAMERA'
  | 'BARCODE'
  | 'HEALTH';

export interface SensorReading {
  id: number;
  sensorType: SensorType;
  sensorKey: string;
  productCode?: string | null;
  machineId?: string | null;
  batchId?: number | null;
  simulationRunId?: number | null;
  value?: number | null;
  unit?: string | null;
  defectCategory?: string | null;
  confidence?: number | null;
  timestamp: string;
}

export interface SensorHealth {
  sensorKey: string;
  sensorType: SensorType;
  online: boolean;
  lastSeenAt?: string | null;
}
