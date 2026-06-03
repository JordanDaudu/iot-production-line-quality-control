import { httpClient } from './httpClient';
import type { SimulationStatus, FaultType } from '../types/simulation';

export interface FaultInjectionResponse {
  faultType: string;
  message: string;
}

// Simulation control. Mutating calls require an Operator or Administrator role on the
// backend; other roles receive HTTP 403.

export async function getSimulationState(): Promise<SimulationStatus> {
  const res = await httpClient.get<SimulationStatus>('/simulation/state');
  return res.data;
}

export async function startSimulation(scenario = 'NORMAL_RUN'): Promise<SimulationStatus> {
  const res = await httpClient.post<SimulationStatus>('/simulation/start', { scenario });
  return res.data;
}

export async function pauseSimulation(): Promise<SimulationStatus> {
  const res = await httpClient.post<SimulationStatus>('/simulation/pause');
  return res.data;
}

export async function stopSimulation(): Promise<SimulationStatus> {
  const res = await httpClient.post<SimulationStatus>('/simulation/stop');
  return res.data;
}

export async function resetSimulation(): Promise<SimulationStatus> {
  const res = await httpClient.post<SimulationStatus>('/simulation/reset');
  return res.data;
}

export async function injectFault(
  faultType: FaultType,
  sensorKey?: string,
  durationSeconds?: number,
): Promise<FaultInjectionResponse> {
  const res = await httpClient.post<FaultInjectionResponse>('/simulation/faults', {
    faultType,
    sensorKey,
    durationSeconds,
  });
  return res.data;
}
