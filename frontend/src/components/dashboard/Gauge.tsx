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
 * A 180° radial gauge whose arc fills to the current value and recolours green →
 * amber → red as it crosses the warn/danger thresholds.
 */
export default function Gauge({ value, min, max, warn, danger, label, unit }: GaugeProps) {
  const cx = 75;
  const cy = 74;
  const r = 56;
  const clamped = Math.max(min, Math.min(max, value));
  const frac = (clamped - min) / (max - min || 1);
  const valAngle = 180 - frac * 180; // 180° (left) → 0° (right)
  const end = polar(cx, cy, r, valAngle);
  const left = polar(cx, cy, r, 180);
  const right = polar(cx, cy, r, 0);

  const color = value >= danger ? 'var(--fail)' : value >= warn ? 'var(--warning)' : 'var(--pass)';

  return (
    <div className="gauge">
      <svg viewBox="0 0 150 90">
        <path
          d={`M ${left.x} ${left.y} A ${r} ${r} 0 0 0 ${right.x} ${right.y}`}
          fill="none"
          stroke="var(--border-bright)"
          strokeWidth="10"
          strokeLinecap="round"
        />
        <path
          d={`M ${left.x} ${left.y} A ${r} ${r} 0 0 0 ${end.x} ${end.y}`}
          fill="none"
          stroke={color}
          strokeWidth="10"
          strokeLinecap="round"
        />
        <text x="75" y="70" textAnchor="middle" fill={color} style={{ font: '600 18px var(--mono)' }}>
          {Number.isFinite(value) ? value.toFixed(1) : '—'}
        </text>
      </svg>
      <div className="gauge-label">
        {label} · {unit}
      </div>
    </div>
  );
}
