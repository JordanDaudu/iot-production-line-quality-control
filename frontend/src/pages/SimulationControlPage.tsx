import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { canControlSimulation } from '../auth/permissions';
import { useSubscription } from '../hooks/useSubscription';
import { Topics } from '../websocket/eventTypes';
import {
  getSimulationState,
  getSimulationRuns,
  startSimulation,
  pauseSimulation,
  stopSimulation,
  resetSimulation,
} from '../api/simulationApi';
import type { SimulationStatus, SimulationState } from '../types/simulation';

const SCENARIOS = [
  'NORMAL_RUN',
  'HIGH_DEFECT_RATE',
  'TEMPERATURE_SPIKE',
  'VIBRATION_FAULT',
  'SENSOR_DISCONNECT',
  'MIXED_FAULT_DEMO',
];

export default function SimulationControlPage() {
  const { user } = useAuth();
  const canControl = !!user && canControlSimulation(user.role);

  const [status, setStatus] = useState<SimulationStatus | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [scenario, setScenario] = useState('NORMAL_RUN');
  const [name, setName] = useState('');
  // Lower-cased names of existing runs, for an instant client-side uniqueness check.
  const [takenNames, setTakenNames] = useState<Set<string>>(new Set());

  useEffect(() => {
    getSimulationState().then(setStatus).catch(() => setError('Could not load simulation state.'));
    refreshRunNames();
  }, []);

  function refreshRunNames() {
    getSimulationRuns()
      .then((runs) => setTakenNames(new Set(runs.map((r) => (r.name ?? '').trim().toLowerCase()).filter(Boolean))))
      .catch(() => undefined);
  }

  // Stay in sync with control actions from any client.
  useSubscription(Topics.SIMULATION_STATE, (message) => {
    setStatus(JSON.parse(message.body) as SimulationStatus);
  });

  async function run(action: () => Promise<SimulationStatus>) {
    setBusy(true);
    setError(null);
    try {
      setStatus(await action());
    } catch (e: unknown) {
      const status = (e as { response?: { status?: number } }).response?.status;
      setError(status === 403 ? 'Your role cannot control the simulation.' : 'Action failed (invalid state?).');
    } finally {
      setBusy(false);
    }
  }

  const state: SimulationState = status?.state ?? 'IDLE';
  // Starting from IDLE/STOPPED creates a new run (name required); from PAUSED it resumes.
  const isNewRun = state === 'IDLE' || state === 'STOPPED';
  const trimmedName = name.trim();
  const nameError: string | null = !isNewRun
    ? null
    : trimmedName === ''
      ? 'A run name is required.'
      : takenNames.has(trimmedName.toLowerCase())
        ? `A run named "${trimmedName}" already exists. Choose a different name.`
        : null;

  async function handleStart() {
    await run(() => startSimulation(isNewRun ? trimmedName : '', scenario));
    setName('');
    refreshRunNames();
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Simulation Control</h2>
        <StatePill state={state} />
      </div>

      <section className="card sim-summary">
        <div>
          <span className="muted">Run</span>
          <strong>{status?.simulationRunId ?? '—'}</strong>
        </div>
        <div>
          <span className="muted">Name</span>
          <strong>{status?.name ?? '—'}</strong>
        </div>
        <div>
          <span className="muted">Scenario</span>
          <strong>{status?.scenario ?? '—'}</strong>
        </div>
        <div>
          <span className="muted">State</span>
          <strong>{state}</strong>
        </div>
      </section>

      {!canControl && (
        <p className="muted">
          You are signed in as <strong>{user?.role}</strong>. Only the Administrator role can
          control the simulation.
        </p>
      )}

      <div className="filter-row">
        <label>
          Run name
          <input
            value={name}
            disabled={!canControl || !isNewRun}
            placeholder="e.g. Line A morning batch"
            onChange={(e) => setName(e.target.value)}
          />
        </label>
        <label>
          Scenario
          <select
            value={scenario}
            disabled={!canControl || state === 'RUNNING' || state === 'PAUSED'}
            onChange={(e) => setScenario(e.target.value)}
          >
            {SCENARIOS.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </label>
      </div>

      {isNewRun && name.trim() !== '' && nameError && <div className="error-text">{nameError}</div>}

      <div className="button-row">
        <button
          disabled={!canControl || busy || state === 'RUNNING' || (isNewRun && nameError !== null)}
          onClick={handleStart}
        >
          {state === 'PAUSED' ? 'Resume' : 'Start'}
        </button>
        <button disabled={!canControl || busy || state !== 'RUNNING'} onClick={() => run(pauseSimulation)}>
          Pause
        </button>
        <button
          disabled={!canControl || busy || (state !== 'RUNNING' && state !== 'PAUSED')}
          onClick={() => run(stopSimulation)}
        >
          Stop
        </button>
        <button className="secondary" disabled={!canControl || busy} onClick={() => run(resetSimulation)}>
          Reset
        </button>
      </div>

      {error && <div className="error-text">{error}</div>}

      <p className="muted">
        Each new run needs a unique name (case-insensitive). Reset clears the live runtime
        state and returns to IDLE; persisted history is kept. Watch the dashboard to see
        readings stream while the simulation runs.
      </p>
    </div>
  );
}

function StatePill({ state }: { state: SimulationState }) {
  return <span className={`state-pill state-${state.toLowerCase()}`}>{state}</span>;
}
