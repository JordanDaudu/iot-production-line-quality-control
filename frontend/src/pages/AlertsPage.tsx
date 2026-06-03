import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useSubscription } from '../hooks/useSubscription';
import { Topics } from '../websocket/eventTypes';
import { getAlerts, acknowledgeAlert, resolveAlert } from '../api/alertsApi';
import type { Alert, AlertStatus } from '../types/alert';

const MANAGE_ROLES = ['MAINTENANCE_TECHNICIAN', 'ADMINISTRATOR'];
const STATUS_OPTIONS: (AlertStatus | 'ALL')[] = ['ALL', 'ACTIVE', 'ACKNOWLEDGED', 'RESOLVED'];

/**
 * Alert history and lifecycle (FR-24). Maintenance Technicians and Administrators can
 * acknowledge and resolve alerts; the backend enforces this regardless of the UI.
 */
export default function AlertsPage() {
  const { user } = useAuth();
  const canManage = !!user && MANAGE_ROLES.includes(user.role);
  const [filter, setFilter] = useState<AlertStatus | 'ALL'>('ACTIVE');
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(() => {
    getAlerts(filter === 'ALL' ? undefined : filter)
      .then(setAlerts)
      .catch(() => setAlerts([]));
  }, [filter]);

  useEffect(() => load(), [load]);
  useSubscription(Topics.ALERTS, () => load());

  async function act(action: () => Promise<unknown>) {
    setError(null);
    try {
      await action();
      load();
    } catch (e: unknown) {
      const status = (e as { response?: { status?: number } }).response?.status;
      setError(status === 403 ? 'Your role cannot manage alerts.' : 'Action failed.');
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Alerts</h2>
        <label className="inline-filter">
          Status
          <select value={filter} onChange={(e) => setFilter(e.target.value as AlertStatus | 'ALL')}>
            {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </label>
      </div>

      {!canManage && <p className="muted">Signed in as {user?.role}. Acknowledge/resolve require Maintenance Technician or Administrator.</p>}
      {error && <div className="error-text">{error}</div>}

      <section className="card">
        {alerts.length === 0 ? (
          <p className="muted">No alerts for this filter.</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr><th>Time</th><th>Type</th><th>Severity</th><th>Status</th><th>Message</th><th>Actions</th></tr>
            </thead>
            <tbody>
              {alerts.map((a) => (
                <tr key={a.id}>
                  <td>{formatTime(a.createdAt)}</td>
                  <td><span className="tag">{a.type}</span></td>
                  <td className={`sev-text sev-${a.severity.toLowerCase()}`}>{a.severity}</td>
                  <td>{a.status}</td>
                  <td className="explanation">{a.message}</td>
                  <td className="actions-cell">
                    <button
                      className="mini"
                      disabled={!canManage || a.status !== 'ACTIVE'}
                      onClick={() => act(() => acknowledgeAlert(a.id))}
                    >
                      Ack
                    </button>
                    <button
                      className="mini secondary"
                      disabled={!canManage || a.status === 'RESOLVED' || a.status === 'CLEARED'}
                      onClick={() => act(() => resolveAlert(a.id))}
                    >
                      Resolve
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}

function formatTime(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString();
}
