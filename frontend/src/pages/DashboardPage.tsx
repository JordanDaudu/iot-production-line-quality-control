import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useStompContext } from '../context/StompContext';
import { useSubscription } from '../hooks/useSubscription';
import { getDashboardSummary } from '../api/dashboardApi';
import { getSimulationState } from '../api/simulationApi';
import { Topics } from '../websocket/eventTypes';
import type { SensorReading } from '../types/sensor';
import type { SimulationStatus, SimulationState } from '../types/simulation';
import type { DashboardSummary } from '../types/dashboard';
import type { QualityStatus } from '../types/inspection';

const MAX_ROWS = 20;

/**
 * Live dashboard. KPIs, latest inspection results, active alerts and sensor health come
 * from the dashboard-summary snapshot (initial REST load + live updates on
 * /topic/dashboard-summary). The raw reading feed comes from /topic/readings.
 */
export default function DashboardPage() {
  const { status } = useStompContext();
  const navigate = useNavigate();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [simulation, setSimulation] = useState<SimulationStatus | null>(null);
  const [readings, setReadings] = useState<SensorReading[]>([]);

  useEffect(() => {
    getDashboardSummary().then(setSummary).catch(() => undefined);
    getSimulationState().then(setSimulation).catch(() => undefined);
  }, []);

  useSubscription(Topics.DASHBOARD_SUMMARY, (m) => setSummary(JSON.parse(m.body) as DashboardSummary));
  useSubscription(Topics.SIMULATION_STATE, (m) => setSimulation(JSON.parse(m.body) as SimulationStatus));
  useSubscription(Topics.READINGS, (m) => {
    const reading = JSON.parse(m.body) as SensorReading;
    setReadings((prev) => [reading, ...prev].slice(0, MAX_ROWS));
  });

  const simState: SimulationState = simulation?.state ?? summary?.simulationState ?? 'IDLE';
  const sensorsOnline = summary ? summary.sensors.filter((s) => s.online).length : 0;

  return (
    <div className="page">
      <div className="page-header">
        <h2>Live Dashboard</h2>
        <ConnectionBadge status={status} />
      </div>

      <section className="kpi-grid">
        <KpiCard label="PASS" value={summary?.passCount ?? 0} tone="pass" />
        <KpiCard label="WARNING" value={summary?.warningCount ?? 0} tone="warning" />
        <KpiCard label="FAIL" value={summary?.failCount ?? 0} tone="fail" />
        <KpiCard label="Active alerts" value={summary?.activeAlertCount ?? 0} tone="neutral" />
      </section>

      <section className="stat-row">
        <StatCard label="Simulation" value={simState} />
        <StatCard label="Total inspected" value={summary?.totalInspected ?? 0} />
        <StatCard label="Sensors online" value={`${sensorsOnline}/${summary?.sensors.length ?? 0}`} />
      </section>

      <div className="dash-grid">
        <section className="card">
          <h3 className="card-title">Latest inspection results</h3>
          {summary && summary.latestResults.length > 0 ? (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>Status</th>
                  <th>Score</th>
                  <th>Explanation</th>
                </tr>
              </thead>
              <tbody>
                {summary.latestResults.map((r) => (
                  <tr key={r.id} className="clickable" onClick={() => navigate(`/products?code=${encodeURIComponent(r.productCode)}`)}>
                    <td>{r.productCode}</td>
                    <td><StatusPill status={r.status} /></td>
                    <td>{r.score ?? '—'}</td>
                    <td className="explanation">{r.explanation}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="muted">No inspection results yet.</p>
          )}
        </section>

        <section className="card">
          <h3 className="card-title">Active alerts</h3>
          {summary && summary.activeAlerts.length > 0 ? (
            <ul className="alert-list">
              {summary.activeAlerts.map((a) => (
                <li key={a.id} className={`alert-item sev-${a.severity.toLowerCase()}`}>
                  <div className="alert-top">
                    <span className="tag">{a.type}</span>
                    <span className="muted">{formatTime(a.createdAt)}</span>
                  </div>
                  <div>{a.message}</div>
                </li>
              ))}
            </ul>
          ) : (
            <p className="muted">No active alerts.</p>
          )}

          <h3 className="card-title sensor-title">Sensor status</h3>
          <div className="sensor-chips">
            {summary?.sensors.map((s) => (
              <span key={s.sensorKey} className={`sensor-chip ${s.online ? 'on' : 'off'}`}>
                {s.sensorKey}
              </span>
            ))}
            {(!summary || summary.sensors.length === 0) && <span className="muted">No sensors yet.</span>}
          </div>
        </section>
      </div>

      <section className="card">
        <h3 className="card-title">Live sensor readings</h3>
        {readings.length === 0 ? (
          <p className="muted">
            No readings yet. Start the simulation from the <strong>Simulation</strong> page.
          </p>
        ) : (
          <table className="data-table">
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
                  <td><span className="tag">{r.sensorType}</span></td>
                  <td>{r.sensorKey}</td>
                  <td>{formatValue(r)}</td>
                  <td>{r.productCode ?? r.machineId ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
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

function KpiCard({
  label,
  value,
  tone,
}: {
  label: string;
  value: number;
  tone: 'pass' | 'warning' | 'fail' | 'neutral';
}) {
  return (
    <div className={`card kpi kpi-${tone}`}>
      <div className="kpi-value">{value}</div>
      <div className="kpi-label">{label}</div>
    </div>
  );
}

function StatCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="card stat-card">
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}

function StatusPill({ status }: { status: QualityStatus }) {
  return <span className={`status-pill status-${status.toLowerCase()}`}>{status}</span>;
}
