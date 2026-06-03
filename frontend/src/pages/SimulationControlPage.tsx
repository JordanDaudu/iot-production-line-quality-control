import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useSubscription } from '../hooks/useSubscription';
import { Topics } from '../websocket/eventTypes';
import {
  getSimulationState,
  startSimulation,
  pauseSimulation,
  stopSimulation,
  resetSimulation,
} from '../api/simulationApi';
import type { SimulationStatus, SimulationState } from '../types/simulation';

const CONTROL_ROLES = ['OPERATOR', 'ADMINISTRATOR'];

export default function SimulationControlPage() {
  const { user } = useAuth();
  const canControl = !!user && CONTROL_ROLES.includes(user.role);

  const [status, setStatus] = useState<SimulationStatus | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    getSimulationState().then(setStatus).catch(() => setError('Could not load simulation state.'));
  }, []);

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
          You are signed in as <strong>{user?.role}</strong>. Only Operator and Administrator
          roles can control the simulation.
        </p>
      )}

      <div className="button-row">
        <button disabled={!canControl || busy || state === 'RUNNING'} onClick={() => run(() => startSimulation())}>
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
        Reset clears the live runtime state and returns to IDLE; persisted history is kept.
        Watch the dashboard to see readings stream while the simulation runs.
      </p>
    </div>
  );
}

function StatePill({ state }: { state: SimulationState }) {
  return <span className={`state-pill state-${state.toLowerCase()}`}>{state}</span>;
}
