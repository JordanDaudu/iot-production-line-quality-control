/**
 * Tiny inline SVG sparkline from a series of numbers.
 */
export default function Sparkline({ data, color }: { data: number[]; color: string }) {
  if (data.length < 2) {
    return <svg className="sparkline" viewBox="0 0 80 26" />;
  }
  const w = 80;
  const h = 26;
  const max = Math.max(...data);
  const min = Math.min(...data);
  const range = max - min || 1;
  const points = data
    .map((v, i) => `${(i / (data.length - 1)) * w},${h - ((v - min) / range) * (h - 4) - 2}`)
    .join(' ');
  return (
    <svg className="sparkline" viewBox={`0 0 ${w} ${h}`} preserveAspectRatio="none">
      <polyline points={points} fill="none" stroke={color} strokeWidth={1.6} strokeLinejoin="round" strokeLinecap="round" />
    </svg>
  );
}
