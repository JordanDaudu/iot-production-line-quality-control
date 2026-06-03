import { useEffect, useMemo, useRef, useState } from 'react';
import { useStompContext } from '../context/StompContext';
import { useSubscription } from '../hooks/useSubscription';
import { getSimulationState } from '../api/simulationApi';
import { Topics } from '../websocket/eventTypes';
import type { SensorReading } from '../types/sensor';
import type { SimulationStatus, SimulationState } from '../types/simulation';

const MAX_ROWS = 25;

/**
 * Live dashboard. Subscribes to the shared STOMP connection and renders sensor readings
 * as they stream in, plus the current simulation state. Quality classification counters
 * (PASS/WARNING/FAIL) and alerts arrive in the next increment.
 */
export default function DashboardPage() {
  const { status } = useStompContext();
  const [readings, setReadings] = useState<SensorReading[]>([]);
  const [simulation, setSimulation] = useState<SimulationStatus | null>(null);
  const totalRef = useRef(0);
  const [total, setTotal] = useState(0);
  const productCodes = useRef<Set<string>>(new Set());
  const [productCount, setProductCount] = useState(0);

  // Initialise the current simulation state on mount; live changes arrive via the
  // subscription below. Without this, the dashboard would show a stale IDLE until the
  // next state change is broadcast.
  useEffect(() => {
    getSimulationState().then(setSimulation).catch(() => undefined);
  }, []);

  useSubscription(Topics.READINGS, (message) => {
    const reading = JSON.parse(message.body) as SensorReading;
    setReadings((prev) => [reading, ...prev].slice(0, MAX_ROWS));
    totalRef.current += 1;
    setTotal(totalRef.current);
    if (reading.productCode) {
      productCodes.current.add(reading.productCode);
      setProductCount(productCodes.current.size);
    }
  });

  useSubscription(Topics.SIMULATION_STATE, (message) => {
    setSimulation(JSON.parse(message.body) as SimulationStatus);
  });

  const simState: SimulationState = simulation?.state ?? 'IDLE';
  const byType = useMemo(() => countByType(readings), [readings]);

  return (
    <div className="page">
      <div className="page-header">
        <h2>Live Dashboard</h2>
        <ConnectionBadge status={status} />
      </div>

      <section className="stat-row">
        <StatCard label="Simulation" value={simState} />
        <StatCard label="Readings (session)" value={total} />
        <StatCard label="Products seen" value={productCount} />
        <StatCard label="Active sensors" value={Object.keys(byType).length} />
      </section>

      <section className="card">
        <h3 className="card-title">Live sensor readings</h3>
        {readings.length === 0 ? (
          <p className="muted">
            No readings yet. Start the simulation from the <strong>Simulation</strong> page to see
            the live IoT stream.
          </p>
        ) : (
          <table className="readings-table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Sensor</th>
                <th>Source</th>
                <th>Value</th>
                <th>Product / Machine</th>
              </tr>
            </thead>
            <tbody>
              {readings.map((r) => (
                <tr key={r.id}>
                  <td>{formatTime(r.timestamp)}</td>
                  <td>
                    <span className="tag">{r.sensorType}</span>
                  </td>
                  <td>{r.sensorKey}</td>
                  <td>{formatValue(r)}</td>
                  <td>{r.productCode ?? r.machineId ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <p className="muted">
        PASS / WARNING / FAIL classification and alerts are added in the next increment.
      </p>
    </div>
  );
}

function countByType(readings: SensorReading[]): Record<string, number> {
  const counts: Record<string, number> = {};
  for (const r of readings) {
    counts[r.sensorType] = (counts[r.sensorType] ?? 0) + 1;
  }
  return counts;
}

function formatValue(r: SensorReading): string {
  if (r.defectCategory) {
    return r.confidence != null ? `${r.defectCategory} (${r.confidence}%)` : r.defectCategory;
  }
  if (r.value != null) {
    return `${r.value}${r.unit ? ' ' + r.unit : ''}`;
  }
  return '—';
}

function formatTime(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleTimeString();
}

function ConnectionBadge({ status }: { status: 'connecting' | 'connected' | 'disconnected' }) {
  const label =
    status === 'connected' ? 'WebSocket connected' : status === 'connecting' ? 'Connecting…' : 'Disconnected';
  return <span className={`conn-badge conn-${status}`}>{label}</span>;
}

function StatCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="card stat-card">
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}
