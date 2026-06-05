import { httpClient } from './httpClient';
import type { InspectionResult } from '../types/inspection';
import type { ProductDetail } from '../types/product';
import type { QualityStatus } from '../types/inspection';
import type { SensorType } from '../types/sensor';

export interface ProductFilters {
  status?: QualityStatus;
  batchId?: number;
  simulationRunId?: number;
  runName?: string;
  from?: string;
  to?: string;
  sensorType?: SensorType;
}

export async function getProducts(filters: ProductFilters = {}): Promise<InspectionResult[]> {
  const params: Record<string, string> = {};
  if (filters.status) params.status = filters.status;
  if (filters.batchId != null) params.batchId = String(filters.batchId);
  if (filters.simulationRunId != null) params.simulationRunId = String(filters.simulationRunId);
  if (filters.runName) params.runName = filters.runName;
  if (filters.from) params.from = filters.from;
  if (filters.to) params.to = filters.to;
  if (filters.sensorType) params.sensorType = filters.sensorType;
  const res = await httpClient.get<InspectionResult[]>('/products', { params });
  return res.data;
}

export async function getProduct(productCode: string): Promise<ProductDetail> {
  const res = await httpClient.get<ProductDetail>(`/products/${encodeURIComponent(productCode)}`);
  return res.data;
}
