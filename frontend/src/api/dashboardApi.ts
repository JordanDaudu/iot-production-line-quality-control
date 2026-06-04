import { httpClient } from './httpClient';
import type { DashboardSummary, TelemetryPoint, SpcChart, DefectCount } from '../types/dashboard';

export async function getDashboardSummary(): Promise<DashboardSummary> {
  const res = await httpClient.get<DashboardSummary>('/dashboard/summary');
  return res.data;
}

/** Machine telemetry history (temperature + vibration) for the active run, from the start. */
export async function getTelemetry(limit = 2000): Promise<TelemetryPoint[]> {
  const res = await httpClient.get<TelemetryPoint[]>('/dashboard/telemetry', { params: { limit } });
  return res.data;
}

/** SPC control-chart data for product weight (samples + control/spec limits). */
export async function getSpc(limit = 100): Promise<SpcChart> {
  const res = await httpClient.get<SpcChart>('/dashboard/spc', { params: { limit } });
  return res.data;
}

/** Visual defect counts (Pareto) for the active run. */
export async function getDefectPareto(): Promise<DefectCount[]> {
  const res = await httpClient.get<DefectCount[]>('/dashboard/defect-pareto');
  return res.data;
}
