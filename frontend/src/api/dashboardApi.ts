import { httpClient } from './httpClient';
import type { DashboardSummary, TelemetryPoint } from '../types/dashboard';

export async function getDashboardSummary(): Promise<DashboardSummary> {
  const res = await httpClient.get<DashboardSummary>('/dashboard/summary');
  return res.data;
}

/** Machine telemetry history (temperature + vibration) for the active run, from the start. */
export async function getTelemetry(limit = 2000): Promise<TelemetryPoint[]> {
  const res = await httpClient.get<TelemetryPoint[]>('/dashboard/telemetry', { params: { limit } });
  return res.data;
}
