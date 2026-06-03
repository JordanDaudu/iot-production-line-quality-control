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
 * A 180° radial gauge. The arc fills to the current value and recolours green → amber →
 * red across the warn/danger thresholds. The numeric readout and label sit in normal
 * flow BELOW the arc, so the value can never be covered by the bar.
 */
export default function Gauge({ value, min, max, warn, danger, label, unit }: GaugeProps) {
  const cx = 70;
  const cy = 60;
  const r = 48;
  const clamped = Math.max(min, Math.min(max, value));
  const frac = (clamped - min) / (max - min || 1);
  const valAngle = 180 - frac * 180; // 180° (left) → 0° (right)
  const end = polar(cx, cy, r, valAngle);
  const left = polar(cx, cy, r, 180);
  const right = polar(cx, cy, r, 0);

  const color = value >= danger ? 'var(--fail)' : value >= warn ? 'var(--warning)' : 'var(--pass)';

  return (
    <div className="gauge">
      <svg className="gauge-svg" viewBox="0 0 140 70">
        <path
          d={`M ${left.x} ${left.y} A ${r} ${r} 0 0 0 ${right.x} ${right.y}`}
          fill="none"
          stroke="var(--border-bright)"
          strokeWidth="9"
          strokeLinecap="round"
        />
        <path
          d={`M ${left.x} ${left.y} A ${r} ${r} 0 0 0 ${end.x} ${end.y}`}
          fill="none"
          stroke={color}
          strokeWidth="9"
          strokeLinecap="round"
        />
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
