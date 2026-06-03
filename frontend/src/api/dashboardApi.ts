import { httpClient } from './httpClient';
import type { DashboardSummary } from '../types/dashboard';

export async function getDashboardSummary(): Promise<DashboardSummary> {
  const res = await httpClient.get<DashboardSummary>('/dashboard/summary');
  return res.data;
}
