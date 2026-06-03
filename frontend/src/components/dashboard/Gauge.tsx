interface GaugeProps {
  value: number;
  min: number;
  max: number;
  warn: number;
  danger: number;
  label: string;
  unit: string;
}

function polar(cx: number, cy: number, r: number, deg: number) {
  const a = (deg * Math.PI) / 180;
  return { x: cx + r * Math.cos(a), y: cy - r * Math.sin(a) };
}

/**
 * Builds the gauge arc as a finely-sampled polyline (sampled every ~3°) rather than an SVG
 * arc command. With rounded joins this draws a perfectly smooth semicircle and avoids the
 * degenerate / flag-direction issues of A-command arcs.
 */
function arcPolyline(cx: number, cy: number, r: number, fromDeg: number, toDeg: number): string {
  const points: string[] = [];
  const total = fromDeg - toDeg;
  const steps = Math.max(2, Math.round(Math.abs(total) / 3));
  for (let i = 0; i <= steps; i++) {
    const deg = fromDeg - (total * i) / steps;
    const p = polar(cx, cy, r, deg);
    points.push(`${p.x.toFixed(2)} ${p.y.toFixed(2)}`);
  }
  return 'M ' + points.join(' L ');
}

/**
 * A 180° radial gauge. The grey track is a full semicircle; the coloured arc fills to the
 * current value and recolours green → amber → red across the warn/danger thresholds. The
 * numeric readout and label sit in normal flow below the arc.
 */
export default function Gauge({ value, min, max, warn, danger, label, unit }: GaugeProps) {
  const cx = 60;
  const cy = 58;
  const r = 50;
  const clamped = Math.max(min, Math.min(max, value));
  const frac = (clamped - min) / (max - min || 1);
  const valAngle = 180 - frac * 180; // 180° (left) → 0° (right)

  const color = value >= danger ? 'var(--fail)' : value >= warn ? 'var(--warning)' : 'var(--pass)';

  return (
    <div className="gauge">
      <svg className="gauge-svg" viewBox="0 0 120 68">
        <path
          d={arcPolyline(cx, cy, r, 180, 0)}
          fill="none"
          stroke="#c4cedd"
          strokeWidth="11"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        {frac > 0.001 && (
          <path
            d={arcPolyline(cx, cy, r, 180, valAngle)}
            fill="none"
            stroke={color}
            strokeWidth="11"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        )}
      </svg>
      <div className="gauge-readout" style={{ color }}>
        {Number.isFinite(value) ? value.toFixed(1) : '—'}
      </div>
      <div className="gauge-label">
        {label} · {unit}
      </div>
    </div>
  );
}
