import { useEffect, useState, type FormEvent } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { getQualitySummary } from '../api/reportsApi';
import type { QualitySummaryReport } from '../types/report';

const COLORS = { PASS: '#22c55e', WARNING: '#f59e0b', FAIL: '#ef4444' };

/**
 * Historical quality report (FR-21): PASS/WARNING/FAIL distribution with an optional
 * date-range filter, shown as a pie chart and summary numbers.
 */
export default function ReportsPage() {
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [batchId, setBatchId] = useState('');
  const [runId, setRunId] = useState('');
  const [report, setReport] = useState<QualitySummaryReport | null>(null);

  function load() {
    getQualitySummary({
      from: toIso(from),
      to: toIso(to),
      batchId: batchId.trim() ? Number(batchId) : undefined,
      simulationRunId: runId.trim() ? Number(runId) : undefined,
    })
      .then(setReport)
      .catch(() => setReport(null));
  }

  useEffect(() => load(), []);

  function onApply(e: FormEvent) {
    e.preventDefault();
    load();
  }

  const data = report
    ? [
        { name: 'PASS', value: report.passCount, color: COLORS.PASS },
        { name: 'WARNING', value: report.warningCount, color: COLORS.WARNING },
        { name: 'FAIL', value: report.failCount, color: COLORS.FAIL },
      ].filter((d) => d.value > 0)
    : [];

  return (
    <div className="page">
      <div className="page-header">
        <h2>Quality Reports</h2>
      </div>

      <section className="card">
        <form className="filter-row" onSubmit={onApply}>
          <label>From<input type="datetime-local" value={from} onChange={(e) => setFrom(e.target.value)} /></label>
          <label>To<input type="datetime-local" value={to} onChange={(e) => setTo(e.target.value)} /></label>
          <label>Batch ID<input className="num-input" value={batchId} onChange={(e) => setBatchId(e.target.value)} /></label>
          <label>Run ID<input className="num-input" value={runId} onChange={(e) => setRunId(e.target.value)} /></label>
          <button type="submit">Apply</button>
          <button
            type="button"
            className="secondary"
            onClick={() => { setFrom(''); setTo(''); setBatchId(''); setRunId(''); setTimeout(load, 0); }}
          >
            Reset
          </button>
        </form>
      </section>

      <div className="dash-grid">
        <section className="card">
          <h3 className="card-title">Distribution</h3>
          {data.length === 0 ? (
            <p className="muted">No data for this range.</p>
          ) : (
            <div style={{ width: '100%', height: 280 }}>
              <ResponsiveContainer>
                <PieChart>
                  <Pie data={data} dataKey="value" nameKey="name" outerRadius={100} label>
                    {data.map((d) => <Cell key={d.name} fill={d.color} />)}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          )}
        </section>

        <section className="card">
          <h3 className="card-title">Summary</h3>
          {report ? (
            <table className="data-table">
              <tbody>
                <tr><td>Total inspected</td><td><strong>{report.total}</strong></td></tr>
                <tr><td className="status-pass">PASS</td><td>{report.passCount} ({report.passRate}%)</td></tr>
                <tr><td className="status-warning">WARNING</td><td>{report.warningCount} ({report.warningRate}%)</td></tr>
                <tr><td className="status-fail">FAIL</td><td>{report.failCount} ({report.failRate}%)</td></tr>
              </tbody>
            </table>
          ) : (
            <p className="muted">No report loaded.</p>
          )}
        </section>
      </div>

      <section className="card">
        <h3 className="card-title">By simulation run</h3>
        {report && report.byRun.length > 0 ? (
          <table className="data-table">
            <thead>
              <tr><th>Run</th><th>Total</th><th className="status-pass">PASS</th><th className="status-warning">WARNING</th><th className="status-fail">FAIL</th></tr>
            </thead>
            <tbody>
              {report.byRun.map((r) => (
                <tr key={r.simulationRunId}>
                  <td>{r.simulationRunId}</td>
                  <td>{r.total}</td>
                  <td>{r.passCount}</td>
                  <td>{r.warningCount}</td>
                  <td>{r.failCount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p className="muted">No runs in range.</p>
        )}
      </section>
    </div>
  );
}

function toIso(localValue: string): string | undefined {
  if (!localValue) return undefined;
  const d = new Date(localValue);
  return Number.isNaN(d.getTime()) ? undefined : d.toISOString();
}
