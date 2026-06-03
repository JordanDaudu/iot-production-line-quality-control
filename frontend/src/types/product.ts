// Mirrors the backend Product entity.
export interface Product {
  id: number;
  productCode: string;
  batchId: number;
  simulationRunId: number;
  createdAt: string;
}
