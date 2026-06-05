import { useEffect, useState, type FormEvent } from 'react';
import { useSearchParams } from 'react-router-dom';
import { getProducts, getProduct, type ProductFilters } from '../api/productsApi';
import type { InspectionResult, QualityStatus } from '../types/inspection';
import type { ProductDetail } from '../types/product';
import type { SensorType } from '../types/sensor';

const STATUS_OPTIONS: (QualityStatus | 'ALL')[] = ['ALL', 'PASS', 'WARNING', 'FAIL'];
const SENSOR_OPTIONS: (SensorType | 'ALL')[] = ['ALL', 'WEIGHT', 'CAMERA', 'BARCODE', 'TEMPERATURE', 'VIBRATION'];

interface FilterForm {
  status: QualityStatus | 'ALL';
  sensorType: SensorType | 'ALL';
  batchId: string;
  simulationRunId: string;
  runName: string;
  from: string;
  to: string;
}

const EMPTY_FILTERS: FilterForm = { status: 'ALL', sensorType: 'ALL', batchId: '', simulationRunId: '', runName: '', from: '', to: '' };

function toIso(localValue: string): string | undefined {
  if (!localValue) return undefined;
  const d = new Date(localValue);
  return Number.isNaN(d.getTime()) ? undefined : d.toISOString();
}

/**
 * Product filtering (FR-14) and traceability drill-down (FR-13, FR-22). Pick a status to
 * filter the list, click a row (or search a Product ID) to see the full record: identity,
 * chronological readings, the inspection result and related alerts.
 */
export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [filters, setFilters] = useState<FilterForm>(EMPTY_FILTERS);
  const [products, setProducts] = useState<InspectionResult[]>([]);
  const [code, setCode] = useState(searchParams.get('code') ?? '');
  const [detail, setDetail] = useState<ProductDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  function loadProducts(active: FilterForm) {
    const query: ProductFilters = {};
    if (active.status !== 'ALL') query.status = active.status;
    if (active.sensorType !== 'ALL') query.sensorType = active.sensorType;
    if (active.batchId.trim()) query.batchId = Number(active.batchId);
    if (active.simulationRunId.trim()) query.simulationRunId = Number(active.simulationRunId);
    if (active.runName.trim()) query.runName = active.runName.trim();
    if (toIso(active.from)) query.from = toIso(active.from);
    if (toIso(active.to)) query.to = toIso(active.to);
    getProducts(query).then(setProducts).catch(() => setProducts([]));
  }

  useEffect(() => {
    loadProducts(EMPTY_FILTERS);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function setField<K extends keyof FilterForm>(key: K, value: FilterForm[K]) {
    setFilters((prev) => ({ ...prev, [key]: value }));
  }

  function applyFilters(e: FormEvent) {
    e.preventDefault();
    loadProducts(filters);
  }

  function resetFilters() {
    setFilters(EMPTY_FILTERS);
    loadProducts(EMPTY_FILTERS);
  }

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
        </form>
        <form className="filter-row" onSubmit={applyFilters}>
          <label>
            Status
            <select value={filters.status} onChange={(e) => setField('status', e.target.value as QualityStatus | 'ALL')}>
              {STATUS_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </label>
          <label>
            Sensor
            <select value={filters.sensorType} onChange={(e) => setField('sensorType', e.target.value as SensorType | 'ALL')}>
              {SENSOR_OPTIONS.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </label>
          <label>Batch ID<input className="num-input" value={filters.batchId} onChange={(e) => setField('batchId', e.target.value)} /></label>
          <label>Run name<input value={filters.runName} onChange={(e) => setField('runName', e.target.value)} placeholder="e.g. Line A morning batch" /></label>
          <label>Run ID<input className="num-input" value={filters.simulationRunId} onChange={(e) => setField('simulationRunId', e.target.value)} /></label>
          <label>From<input type="datetime-local" value={filters.from} onChange={(e) => setField('from', e.target.value)} /></label>
          <label>To<input type="datetime-local" value={filters.to} onChange={(e) => setField('to', e.target.value)} /></label>
          <button type="submit">Apply</button>
          <button type="button" className="secondary" onClick={resetFilters}>Reset</button>
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

        <section className="card detail-sticky">
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
        <span><span className="muted">Run name</span><strong>{detail.simulationRunName ?? '—'}</strong></span>
        <span><span className="muted">Run ID</span><strong>{detail.simulationRunId}</strong></span>
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
