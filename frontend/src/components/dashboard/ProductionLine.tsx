import type { InspectionResult } from '../../types/inspection';

/**
 * Animated conveyor of recently inspected products. Each result slides in from the right
 * as a colour-coded tile; FAIL tiles pulse. `items` is newest-first.
 */
export default function ProductionLine({ items }: { items: InspectionResult[] }) {
  const ordered = items.slice().reverse(); // oldest left → newest right
  return (
    <div className="prod-line">
      <div className="prod-line-label">Production line — live inspection feed →</div>
      <div className="prod-line-rail">
        {ordered.length === 0 && (
          <span className="prod-line-empty muted">Awaiting products… start the simulation.</span>
        )}
        {ordered.map((r) => (
          <div key={r.id} className={`prod-tile tile-${r.status.toLowerCase()}`} title={r.explanation}>
            <span className="prod-code">{r.productCode}</span>
            <span className="prod-status">{r.status}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
