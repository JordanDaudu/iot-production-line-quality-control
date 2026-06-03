import { useEffect, useState, type ReactNode } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useSubscription } from '../../hooks/useSubscription';
import { Topics } from '../../websocket/eventTypes';
import { getSimulationState } from '../../api/simulationApi';
import type { SimulationState, SimulationStatus } from '../../types/simulation';
import LiveBeacon from './LiveBeacon';

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/products', label: 'Products', end: false },
  { to: '/alerts', label: 'Alerts', end: false },
  { to: '/reports', label: 'Reports', end: false },
  { to: '/simulation', label: 'Simulation', end: false },
  { to: '/settings', label: 'Settings', end: false },
];

/**
 * Shared application chrome: a state-tinted top bar with the live beacon and active
 * user/role, a left nav, and global alert toasts.
 */
export default function AppLayout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();
  const [simState, setSimState] = useState<SimulationState>('IDLE');

  useEffect(() => {
    getSimulationState().then((s) => setSimState(s.state)).catch(() => undefined);
  }, []);

  useSubscription(Topics.SIMULATION_STATE, (m) => {
    setSimState((JSON.parse(m.body) as SimulationStatus).state);
  });

  return (
    <div className="app-shell" data-sim-state={simState}>
      <header className="app-header">
        <div className="brand">Smart IoT · Quality Inspection</div>
        <div className="header-right">
          <LiveBeacon />
          {user && (
            <span className="role-badge" title={user.username}>
              {user.displayName} · {user.role}
            </span>
          )}
          <button className="link" onClick={logout}>
            Sign out
          </button>
        </div>
      </header>

      <div className="app-body">
        <nav className="app-nav">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <main className="app-content">{children}</main>
      </div>
    </div>
  );
}
