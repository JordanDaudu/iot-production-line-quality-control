// Mirrors the backend QualityStatus enum and InspectionResult entity.
export type QualityStatus = 'PASS' | 'WARNING' | 'FAIL';

export interface InspectionResult {
  id: number;
  productCode: string;
  batchId: number;
  simulationRunId: number;
  status: QualityStatus;
  score?: number | null;
  explanation: string;
  createdAt: string;
}
