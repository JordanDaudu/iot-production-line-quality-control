import type { SensorType } from './sensor';

// Mirrors the backend ThresholdDto.
export interface Threshold {
  sensorType: SensorType;
  minValue: number;
  warnMinValue: number;
  warnMaxValue: number;
  maxValue: number;
  unit: string;
  updatedAt?: string | null;
  updatedByRole?: string | null;
}

export interface ThresholdUpdate {
  minValue: number;
  warnMinValue: number;
  warnMaxValue: number;
  maxValue: number;
  unit: string;
}
