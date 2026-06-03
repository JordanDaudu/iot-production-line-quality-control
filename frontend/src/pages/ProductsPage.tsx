import { useEffect, useState, type FormEvent } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getProducts, getProduct } from '../api/productsApi';
import type { InspectionResult, QualityStatus } from '../types/inspection';
import type { ProductDetail } from '../types/product';

const STATUS_OPTIONS: (QualityStatus | 'ALL')[] = ['ALL', 'PASS', 'WARNING', 'FAIL'];

/**
 * Product filtering (FR-14) and traceability drill-down (FR-13, FR-22). Pick a status to
 * filter the list, click a row (or search a Product ID) to see the full record: identity,
 * chronological readings, the inspection result and related alerts.
 */
export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [statusFilter, setStatusFilter] = useState<QualityStatus | 'ALL'>('ALL');
  const [products, setProducts] = useState<InspectionResult[]>([]);
  const [code, setCode] = useState(searchParams.get('code') ?? '');
  const [detail, setDetail] = useState<ProductDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const filters = statusFilter === 'ALL' ? {} : { status: statusFilter };
    getProducts(filters).then(setProducts).catch(() => setProducts([]));
  }, [statusFilter]);

  useEffect(() => {
    const initial = searchParams.get('code');
    if (initial) {
      loadDetail(initial);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function loadDetail(productCode: string) {
    setError(null);
    try {
      const d = await getProduct(productCode);
      setDetail(d);
      setCode(productCode);
      setSearchParams({ code: productCode });
    } catch {
      setDetail(null);
      setError(`No product found with id "${productCode}".`);
    }
  }

  function onSearch(e: FormEvent) {
    e.preventDefault();
    if (code.trim()) {
      loadDetail(code.trim());
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h2>Products &amp; Traceability</h2>
      </div>

      <section className="card">
        <form className="filter-row" onSubmit={onSearch}>
          <label>
            Product ID
            <input value={code} onChange={(e) => setCode(e.target.value)} placeholder="BATCH001-P0001" />
          </label>
          <button type="submit">Search</button>
          <label>
            Status
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as QualityStatus | 'ALL')}>
              {STATUS_OPTIONS.map((s) => (
                <option key={s} value={s}>{s}</option>
              ))}
            </select>
          </label>
        </form>
        {error && <div className="error-text">{error}</div>}
      </section>

      <div className="dash-grid">
        <section className="card">
          <h3 className="card-title">Inspected products ({products.length})</h3>
          {products.length === 0 ? (
            <p className="muted">No products match the filter.</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr><th>Product</th><th>Status</th><th>Score</th></tr>
              </thead>
              <tbody>
                {products.map((p) => (
                  <tr
                    key={p.id}
                    className={`clickable ${detail?.productCode === p.productCode ? 'selected' : ''}`}
                    onClick={() => loadDetail(p.productCode)}
                  >
                    <td>{p.productCode}</td>
                    <td><StatusPill status={p.status} /></td>
                    <td>{p.score ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        <section className="card">
          <h3 className="card-title">Product detail</h3>
          {!detail ? (
            <p className="muted">Select a product or search a Product ID.</p>
          ) : (
            <ProductDetailView detail={detail} />
          )}
        </section>
      </div>
    </div>
  );
}

function ProductDetailView({ detail }: { detail: ProductDetail }) {
  return (
    <div className="product-detail">
      <div className="detail-meta">
        <span><span className="muted">Product</span><strong>{detail.productCode}</strong></span>
        <span><span className="muted">Batch</span><strong>{detail.batchCode ?? detail.batchId}</strong></span>
        <span><span className="muted">Run</span><strong>{detail.simulationRunId}</strong></span>
        <span><span className="muted">Scenario</span><strong>{detail.scenario ?? '—'}</strong></span>
      </div>

      {detail.result && (
        <div className="result-box">
          <StatusPill status={detail.result.status} />
          <span className="muted"> score {detail.result.score ?? '—'}</span>
          <p>{detail.result.explanation}</p>
        </div>
      )}

      <h4 className="sub-title">Readings ({detail.readings.length})</h4>
      <table className="data-table">
        <thead>
          <tr><th>Time</th><th>Sensor</th><th>Value</th></tr>
        </thead>
        <tbody>
          {detail.readings.map((r) => (
            <tr key={r.id}>
              <td>{formatTime(r.timestamp)}</td>
              <td><span className="tag">{r.sensorType}</span></td>
              <td>{r.defectCategory ?? (r.value != null ? `${r.value}${r.unit ? ' ' + r.unit : ''}` : '—')}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <h4 className="sub-title">Related alerts ({detail.alerts.length})</h4>
      {detail.alerts.length === 0 ? (
        <p className="muted">None.</p>
      ) : (
        <ul className="alert-list">
          {detail.alerts.map((a) => (
            <li key={a.id} className={`alert-item sev-${a.severity.toLowerCase()}`}>
              <span className="tag">{a.type}</span> {a.message}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

function StatusPill({ status }: { status: QualityStatus }) {
  return <span className={`status-pill status-${status.toLowerCase()}`}>{status}</span>;
}

function formatTime(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleTimeString();
}
