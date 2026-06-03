import type { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/products', label: 'Products', end: false },
  { to: '/alerts', label: 'Alerts', end: false },
  { to: '/reports', label: 'Reports', end: false },
  { to: '/simulation', label: 'Simulation', end: false },
  { to: '/settings', label: 'Settings', end: false },
];

/**
 * Shared application chrome: top bar with the active user/role and a left nav.
 * Nav targets beyond Dashboard are wired up in later increments.
 */
export default function AppLayout({ children }: { children: ReactNode }) {
  const { user, logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand">Smart IoT Quality Inspection</div>
        <div className="header-right">
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
