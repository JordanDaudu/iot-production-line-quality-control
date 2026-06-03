import { httpClient } from './httpClient';
import type { Alert, AlertStatus } from '../types/alert';

export async function getAlerts(status?: AlertStatus): Promise<Alert[]> {
  const params = status ? { status } : undefined;
  const res = await httpClient.get<Alert[]>('/alerts', { params });
  return res.data;
}

export async function acknowledgeAlert(id: number, note?: string): Promise<Alert> {
  const res = await httpClient.post<Alert>(`/alerts/${id}/acknowledge`, note ? { note } : {});
  return res.data;
}

export async function resolveAlert(id: number): Promise<Alert> {
  const res = await httpClient.post<Alert>(`/alerts/${id}/resolve`);
  return res.data;
}
