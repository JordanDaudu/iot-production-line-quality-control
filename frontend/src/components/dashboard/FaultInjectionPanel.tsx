import { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { injectFault } from '../../api/simulationApi';
import type { FaultType, SimulationState } from '../../types/simulation';

// The four fixed simulated sensors (see backend SensorSimulationService).
const SENSORS = ['WEIGHT-1', 'CAMERA-1', 'TEMPERATURE-1', 'VIBRATION-1'];

const FAULTS: { type: FaultType; label: string; detail: string }[] = [
  { type: 'OVERWEIGHT_PRODUCT', label: 'Overweight product', detail: 'Next product weight forced over the limit (expected FAIL).' },
  { type: 'VISUAL_DEFECT', label: 'Visual defect (crack)', detail: 'Next camera result forced to a critical crack (expected FAIL).' },
  { type: 'TEMPERATURE_SPIKE', label: 'Temperature spike', detail: 'Next temperature reading forced over the limit (maintenance alert).' },
  { type: 'VIBRATION_SPIKE', label: 'Vibration spike', detail: 'Next vibration reading forced over the limit (maintenance alert).' },
  { type: 'SENSOR_DISCONNECT', label: 'Disconnect sensor', detail: 'Suppress a sensor for a while (expected offline + sensor-health alert).' },
];

/**
 * Admin-only fault-injection control surfaced on the live dashboard (FR-20). Triggers a
 * simulated fault on the next production cycle so the operator can watch the resulting
 * alerts update in the cards beside it. Renders nothing for non-admins; the injection
 * endpoint is itself admin-guarded server-side (NFR-11).
 */
export default function FaultInjectionPanel({ simState }: { simState: SimulationState }) {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMINISTRATOR';

  const [message, setMessage] = useState<string | null>(null);
  const [isError, setIsError] = useState(false);
  // Options for SENSOR_DISCONNECT (the backend defaults these when blank).
  const [sensorKey, setSensorKey] = useState('VIBRATION-1');
  const [durationSeconds, setDurationSeconds] = useState('20');

  if (!isAdmin) return null;

  const running = simState === 'RUNNING';

  async function fire(faultType: FaultType) {
    setMessage(null);
    setIsError(false);
    try {
      const key = sensorKey.trim() || undefined;
      const secs = Number(durationSeconds);
      const duration = faultType === 'SENSOR_DISCONNECT' && secs > 0 ? secs : undefined;
      const res = await injectFault(faultType, faultType === 'SENSOR_DISCONNECT' ? key : undefined, duration);
      setMessage(res.message);
    } catch (e: unknown) {
      const s = (e as { response?: { status?: number } }).response?.status;
      setIsError(true);
      setMessage(s === 403 ? 'Administrator role required for fault injection.' : 'Fault injection failed.');
    }
  }

  return (
    <section className="card">
      <div className="card-head">
        <h3 className="card-title">Fault injection</h3>
        <span className="tag admin-tag">Admin</span>
      </div>
      <p className="muted">
        Trigger a simulated fault on the next production cycle and watch the alerts update above.
        {running ? '' : ' Start the simulation first — faults apply on the next running cycle.'}
      </p>

      <div className="filter-row">
        <label>
          Disconnect target (sensor)
          <select value={sensorKey} onChange={(e) => setSensorKey(e.target.value)}>
            {SENSORS.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </label>
        <label>
          Duration (s)
          <input
            className="num-input"
            type="number"
            min={1}
            value={durationSeconds}
            onChange={(e) => setDurationSeconds(e.target.value)}
          />
        </label>
      </div>

      <div className="button-row">
        {FAULTS.map((f) => (
          <button key={f.type} className="secondary" disabled={!running} title={f.detail} onClick={() => fire(f.type)}>
            {f.label}
          </button>
        ))}
      </div>

      {message && <div className={isError ? 'error-text' : 'ok-text'}>{message}</div>}
    </section>
  );
}
