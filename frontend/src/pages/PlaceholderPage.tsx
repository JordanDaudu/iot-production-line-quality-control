/**
 * Generic placeholder for pages that arrive in a later increment, so navigation targets
 * resolve cleanly instead of silently redirecting.
 */
export default function PlaceholderPage({ title }: { title: string }) {
  return (
    <div className="page">
      <div className="page-header">
        <h2>{title}</h2>
      </div>
      <p className="muted">This section is coming in a later increment.</p>
    </div>
  );
}
