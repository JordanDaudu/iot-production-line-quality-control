import { httpClient } from './httpClient';
import type { QualitySummaryReport } from '../types/report';

export interface ReportFilters {
  batchId?: number;
  simulationRunId?: number;
  from?: string;
  to?: string;
}

export async function getQualitySummary(filters: ReportFilters = {}): Promise<QualitySummaryReport> {
  const params: Record<string, string> = {};
  if (filters.batchId != null) params.batchId = String(filters.batchId);
  if (filters.simulationRunId != null) params.simulationRunId = String(filters.simulationRunId);
  if (filters.from) params.from = filters.from;
  if (filters.to) params.to = filters.to;
  const res = await httpClient.get<QualitySummaryReport>('/reports/quality-summary', { params });
  return res.data;
}
