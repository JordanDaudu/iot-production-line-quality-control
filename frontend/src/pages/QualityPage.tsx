import { useEffect, useState } from 'react';
import {
  ComposedChart,
  LineChart,
  Line,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  ReferenceLine,
} from 'recharts';
import { useSubscription } from '../hooks/useSubscription';
import { Topics } from '../websocket/eventTypes';
import { getSpc, getDefectPareto } from '../api/dashboardApi';
import type { SpcChart, DefectCount } from '../types/dashboard';

/**
 * Quality analytics: an SPC (statistical process control) chart of product weight with
 * ±3σ control limits, centre line and spec limits — out-of-control points are flagged —
 * plus a defect Pareto chart. Both refresh live as products are inspected.
 */
export default function QualityPage() {
  const [spc, setSpc] = useState<SpcChart | null>(null);
  const [pareto, setPareto] = useState<DefectCount[]>([]);

  function load() {
    getSpc().then(setSpc).catch(() => undefined);
    getDefectPareto().then(setPareto).catch(() => setPareto([]));
  }

  useEffect(() => load(), []);
  useSubscription(Topics.DASHBOARD_SUMMARY, () => load());

  const total = pareto.reduce((s, d) => s + d.count, 0);
  let running = 0;
  const paretoData = pareto.map((d) => {
    running += d.count;
    return { category: d.category, count: d.count, cumulative: total ? Math.round((running / total) * 100) : 0 };
  });

  return (
    <div className="page">
      <div className="page-header">
        <h2>Quality Analytics</h2>
      </div>

      <section className="card">
        <h3 className="card-title">Weight control chart (SPC)</h3>
        {spc && spc.points.length > 1 ? (
          <>
            <div style={{ width: '100%', height: 320 }}>
              <ResponsiveContainer>
                <LineChart data={spc.points} margin={{ top: 10, right: 48, bottom: 5, left: -6 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                  <XAxis dataKey="index" tick={{ fontSize: 10, fill: '#647386' }} minTickGap={40} />
                  <YAxis tick={{ fontSize: 10, fill: '#647386' }} domain={spcDomain(spc)} allowDecimals={false} />
                  <Tooltip contentStyle={{ background: '#fff', border: '1px solid #dde4ee' }} />
                  <ReferenceLine y={spc.specHigh} stroke="#d4860f" strokeDasharray="2 4" />
                  <ReferenceLine y={spc.specLow} stroke="#d4860f" strokeDasharray="2 4" />
                  <ReferenceLine y={spc.ucl} stroke="#e0394a" strokeDasharray="5 4"
                    label={{ value: `UCL ${spc.ucl}`, fontSize: 10, fill: '#e0394a', position: 'right' }} />
                  <ReferenceLine y={spc.lcl} stroke="#e0394a" strokeDasharray="5 4"
                    label={{ value: `LCL ${spc.lcl}`, fontSize: 10, fill: '#e0394a', position: 'right' }} />
                  <ReferenceLine y={spc.centerLine} stroke="#15a35a"
                    label={{ value: `x̄ ${spc.centerLine}`, fontSize: 10, fill: '#15a35a', position: 'right' }} />
                  <Line type="monotone" dataKey="value" stroke="#0e8ba8" strokeWidth={1.5} dot={<SpcDot />} isAnimationActive={false} name="Weight" />
                </LineChart>
              </ResponsiveContainer>
            </div>
            <p className="muted">
              Centre x̄ = {spc.centerLine} {spc.unit} · control limits {spc.lcl}–{spc.ucl} {spc.unit} (±3σ) ·
              spec {spc.specLow}–{spc.specHigh} {spc.unit}. Red points are out of statistical control.
            </p>
          </>
        ) : (
          <p className="muted">No weight samples yet. Start the simulation to populate the control chart.</p>
        )}
      </section>

      <section className="card">
        <h3 className="card-title">Defect Pareto</h3>
        {paretoData.length > 0 ? (
          <div style={{ width: '100%', height: 300 }}>
            <ResponsiveContainer>
              <ComposedChart data={paretoData} margin={{ top: 10, right: 16, bottom: 5, left: -6 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="category" tick={{ fontSize: 10, fill: '#647386' }} />
                <YAxis yAxisId="left" tick={{ fontSize: 10, fill: '#647386' }} allowDecimals={false} />
                <YAxis yAxisId="right" orientation="right" domain={[0, 100]} tick={{ fontSize: 10, fill: '#647386' }} unit="%" />
                <Tooltip contentStyle={{ background: '#fff', border: '1px solid #dde4ee' }} />
                <Legend />
                <Bar yAxisId="left" dataKey="count" fill="#0e8ba8" name="Count" radius={[3, 3, 0, 0]} />
                <Line yAxisId="right" type="monotone" dataKey="cumulative" stroke="#d4860f" strokeWidth={2} name="Cumulative %" />
              </ComposedChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <p className="muted">No defects recorded yet.</p>
        )}
      </section>
    </div>
  );
}

function spcDomain(spc: SpcChart): [number, number] {
  const values = spc.points.map((p) => p.value);
  const lo = Math.min(spc.lcl, spc.specLow, ...values);
  const hi = Math.max(spc.ucl, spc.specHigh, ...values);
  const pad = Math.max(1, (hi - lo) * 0.08);
  return [Math.floor(lo - pad), Math.ceil(hi + pad)];
}

interface DotProps {
  cx?: number;
  cy?: number;
  payload?: { outOfControl?: boolean };
}

function SpcDot({ cx = 0, cy = 0, payload }: DotProps) {
  const out = !!payload?.outOfControl;
  return (
    <circle cx={cx} cy={cy} r={out ? 4.5 : 2.5} fill={out ? '#e0394a' : '#0e8ba8'} stroke={out ? '#e0394a' : 'none'} />
  );
}
