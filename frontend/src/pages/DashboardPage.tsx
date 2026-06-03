import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { useStompContext } from '../context/StompContext';
import { useSubscription } from '../hooks/useSubscription';
import { useCountUp } from '../hooks/useCountUp';
import { getDashboardSummary } from '../api/dashboardApi';
import { getSimulationState } from '../api/simulationApi';
import { Topics } from '../websocket/eventTypes';
import ProductionLine from '../components/dashboard/ProductionLine';
import Sparkline from '../components/dashboard/Sparkline';
import Gauge from '../components/dashboard/Gauge';
import type { SensorReading } from '../types/sensor';
import type { SimulationStatus, SimulationState } from '../types/simulation';
import type { DashboardSummary } from '../types/dashboard';
import type { InspectionResult, QualityStatus } from '../types/inspection';

const MAX_ROWS = 18;
const HIST = 24;

/**
 * Mission-control dashboard: animated production line, count-up KPIs with sparklines,
 * machine gauges + trend, active alerts, sensor health and live feeds.
 */
export default function DashboardPage() {
  const { status } = useStompContext();
  const navigate = useNavigate();
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [simulation, setSimulation] = useState<SimulationStatus | null>(null);
  const [readings, setReadings] = useState<SensorReading[]>([]);
  const [prodItems, setProdItems] = useState<InspectionResult[]>([]);
  const [hist, setHist] = useState<{ pass: number[]; warning: number[]; fail: number[]; alerts: number[] }>({
    pass: [],
    warning: [],
    fail: [],
    alerts: [],
  });
  const lastTempRef = useRef<number | null>(null);
  const [gaugeTemp, setGaugeTemp] = useState(0);
  const [gaugeVib, setGaugeVib] = useState(0);
  const [trend, setTrend] = useState<{ t: string; temperature: number | null; vibration: number | null }[]>([]);

  function applySummary(s: DashboardSummary) {
    setSummary(s);
    setHist((h) => ({
      pass: [...h.pass, s.passCount].slice(-HIST),
      warning: [...h.warning, s.warningCount].slice(-HIST),
      fail: [...h.fail, s.failCount].slice(-HIST),
      alerts: [...h.alerts, s.activeAlertCount].slice(-HIST),
    }));
  }

  useEffect(() => {
    getDashboardSummary().then(applySummary).catch(() => undefined);
    getSimulationState().then(setSimulation).catch(() => undefined);
  }, []);

  useSubscription(Topics.DASHBOARD_SUMMARY, (m) => applySummary(JSON.parse(m.body) as DashboardSummary));
  useSubscription(Topics.SIMULATION_STATE, (m) => setSimulation(JSON.parse(m.body) as SimulationStatus));
  useSubscription(Topics.INSPECTION_RESULTS, (m) => {
    const r = JSON.parse(m.body) as InspectionResult;
    setProdItems((prev) => [r, ...prev].slice(0, 14));
  });
  useSubscription(Topics.READINGS, (m) => {
    const reading = JSON.parse(m.body) as SensorReading;
    setReadings((prev) => [reading, ...prev].slice(0, MAX_ROWS));
    if (reading.sensorType === 'TEMPERATURE' && reading.value != null) {
      lastTempRef.current = reading.value;
      setGaugeTemp(reading.value);
    } else if (reading.sensorType === 'VIBRATION' && reading.value != null) {
      const vib = reading.value;
      setGaugeVib(vib);
      setTrend((prev) =>
        [...prev, { t: formatTime(reading.timestamp), temperature: lastTempRef.current, vibration: vib }].slice(-20),
      );
    }
  });

  const simState: SimulationState = simulation?.state ?? summary?.simulationState ?? 'IDLE';
  const sensorsOnline = summary ? summary.sensors.filter((s) => s.online).length : 0;

  return (
    <div className="page">
      <div className="page-header">
        <h2>Live Dashboard</h2>
        <ConnectionBadge status={status} />
      </div>

      <ProductionLine items={prodItems} />

      <section className="kpi-grid">
        <KpiCard label="Pass" value={summary?.passCount ?? 0} tone="pass" history={hist.pass} />
        <KpiCard label="Warning" value={summary?.warningCount ?? 0} tone="warning" history={hist.warning} />
        <KpiCard label="Fail" value={summary?.failCount ?? 0} tone="fail" history={hist.fail} />
        <KpiCard label="Active alerts" value={summary?.activeAlertCount ?? 0} tone="neutral" history={hist.alerts} />
      </section>

      <section className="stat-row">
        <StatCard label="Simulation" value={simState} />
        <StatCard label="Total inspected" value={summary?.totalInspected ?? 0} />
        <StatCard label="Sensors online" value={`${sensorsOnline}/${summary?.sensors.length ?? 0}`} />
        <StatCard label="Scenario" value={simulation?.scenario ?? '—'} />
      </section>

      <div className="dash-grid">
        <section className="card">
          <h3 className="card-title">Machine telemetry</h3>
          <div className="gauge-row">
            <Gauge value={gaugeTemp} min={10} max={45} warn={30} danger={35} label="Temperature" unit="°C" />
            <Gauge value={gaugeVib} min={0} max={12} warn={5} danger={8} label="Vibration" unit="mm/s" />
          </div>
          {trend.length > 1 && (
            <div style={{ width: '100%', height: 180, marginTop: '0.5rem' }}>
              <ResponsiveContainer>
                <LineChart data={trend} margin={{ top: 5, right: 16, bottom: 5, left: -12 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#1d2a3a" />
                  <XAxis dataKey="t" tick={{ fontSize: 10, fill: '#6b7d92' }} />
                  <YAxis tick={{ fontSize: 10, fill: '#6b7d92' }} />
                  <Tooltip contentStyle={{ background: '#0b121b', border: '1px solid #25384c' }} />
                  <Legend />
                  <Line type="monotone" dataKey="temperature" stroke="#2dd4ee" dot={false} name="Temp (°C)" />
                  <Line type="monotone" dataKey="vibration" stroke="#f7b733" dot={false} name="Vibration" />
                </LineChart>
              </ResponsiveContainer>
            </div>
          )}
        </section>

        <section className="card">
          <h3 className="card-title">Active alerts</h3>
          {summary && summary.activeAlerts.length > 0 ? (
            <ul className="alert-list">
              {summary.activeAlerts.slice(0, 6).map((a) => (
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
        <h3 className="card-title">Latest inspection results</h3>
        {summary && summary.latestResults.length > 0 ? (
          <table className="data-table">
            <thead>
              <tr><th>Product</th><th>Status</th><th>Score</th><th>Explanation</th></tr>
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
        <h3 className="card-title">Live sensor readings</h3>
        {readings.length === 0 ? (
          <p className="muted">No readings yet. Start the simulation from the <strong>Simulation</strong> page.</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr><th>Time</th><th>Sensor</th><th>Source</th><th>Value</th><th>Product / Machine</th></tr>
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

const TONE_COLOR = { pass: '#3ddc97', warning: '#f7b733', fail: '#ff5d6c', neutral: '#2dd4ee' } as const;

function KpiCard({
  label,
  value,
  tone,
  history,
}: {
  label: string;
  value: number;
  tone: 'pass' | 'warning' | 'fail' | 'neutral';
  history: number[];
}) {
  const display = useCountUp(value);
  return (
    <div className={`card kpi kpi-${tone}`}>
      <div className="kpi-top">
        <div className="kpi-value">{display}</div>
        <Sparkline data={history} color={TONE_COLOR[tone]} />
      </div>
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
