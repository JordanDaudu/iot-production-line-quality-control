import { httpClient } from './httpClient';
import type { Threshold, ThresholdUpdate } from '../types/threshold';
import type { SensorType } from '../types/sensor';

export async function getThresholds(): Promise<Threshold[]> {
  const res = await httpClient.get<Threshold[]>('/thresholds');
  return res.data;
}

export async function updateThreshold(sensorType: SensorType, body: ThresholdUpdate): Promise<Threshold> {
  const res = await httpClient.put<Threshold>(`/thresholds/${sensorType}`, body);
  return res.data;
}
