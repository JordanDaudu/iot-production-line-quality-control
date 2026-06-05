import type { InspectionResult } from './inspection';
import type { SensorReading } from './sensor';
import type { Alert } from './alert';

// Mirrors the backend Product entity.
export interface Product {
  id: number;
  productCode: string;
  batchId: number;
  simulationRunId: number;
  createdAt: string;
}

// Mirrors the backend ProductDetailDto (full traceability view).
export interface ProductDetail {
  productCode: string;
  batchId: number;
  batchCode: string | null;
  simulationRunId: number;
  simulationRunName: string | null;
  scenario: string | null;
  createdAt: string;
  result: InspectionResult | null;
  readings: SensorReading[];
  alerts: Alert[];
}
