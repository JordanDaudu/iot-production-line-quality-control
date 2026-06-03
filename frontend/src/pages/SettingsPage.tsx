import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getThresholds, updateThreshold } from '../api/thresholdsApi';
import type { Threshold } from '../types/threshold';
import type { SensorType } from '../types/sensor';

/**
 * Threshold configuration (FR-18). Administrators can edit the quality limits per sensor;
 * changes apply to new inspections immediately. Other roles see a read-only view.
 */
export default function SettingsPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMINISTRATOR';
  const [drafts, setDrafts] = useState<Threshold[]>([]);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  function load() {
    getThresholds().then(setDrafts).catch(() => setDrafts([]));
  }

  useEffect(() => load(), []);

  function setField(type: SensorType, field: keyof Threshold, value: string) {
    setDrafts((prev) => prev.map((t) => (t.sensorType === type ? { ...t, [field]: value } : t)));
  }

  async function save(t: Threshold) {
    setError(null);
    setMessage(null);
    try {
      await updateThreshold(t.sensorType, {
        minValue: Number(t.minValue),
        warnMinValue: Number(t.warnMinValue),
        warnMaxValue: Number(t.warnMaxValue),
        maxValue: Number(t.maxValue),
        unit: t.unit,
      });
      setMessage(`${t.sensorType} thresholds saved.`);
      load();
    } catch (e: unknown) {
      const status = (e as { response?: { status?: number } }).response?.status;
      setError(
        status === 400
          ? 'Invalid: require min ≤ warnMin ≤ warnMax ≤ max.'
          : status === 403
          ? 'Administrator role required.'
          : 'Save failed.',
      );
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Settings — Quality Thresholds</h2>
      </div>

      {!isAdmin && <p className="muted">Signed in as {user?.role}. Only Administrators can edit thresholds.</p>}
      {message && <div className="ok-text">{message}</div>}
      {error && <div className="error-text">{error}</div>}

      <section className="card">
        <table className="data-table">
          <thead>
            <tr><th>Sensor</th><th>Min</th><th>Warn Min</th><th>Warn Max</th><th>Max</th><th>Unit</th>{isAdmin && <th></th>}</tr>
          </thead>
          <tbody>
            {drafts.map((t) => (
              <tr key={t.sensorType}>
                <td><span className="tag">{t.sensorType}</span></td>
                {(['minValue', 'warnMinValue', 'warnMaxValue', 'maxValue'] as (keyof Threshold)[]).map((field) => (
                  <td key={field}>
                    {isAdmin ? (
                      <input
                        className="num-input"
                        value={String(t[field])}
                        onChange={(e) => setField(t.sensorType, field, e.target.value)}
                      />
                    ) : (
                      String(t[field])
                    )}
                  </td>
                ))}
                <td>
                  {isAdmin ? (
                    <input className="unit-input" value={t.unit} onChange={(e) => setField(t.sensorType, 'unit', e.target.value)} />
                  ) : (
                    t.unit
                  )}
                </td>
                {isAdmin && (
                  <td><button className="mini" onClick={() => save(t)}>Save</button></td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
        <p className="muted">Bands: &lt; Min = FAIL · Min–WarnMin = WARNING · WarnMin–WarnMax = PASS · WarnMax–Max = WARNING · &gt; Max = FAIL.</p>
      </section>
    </div>
  );
}
