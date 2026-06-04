import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { StompProvider } from '../context/StompContext';
import AppLayout from '../components/layout/AppLayout';
import LoginPage from '../pages/LoginPage';
import DashboardPage from '../pages/DashboardPage';
import ProductsPage from '../pages/ProductsPage';
import QualityPage from '../pages/QualityPage';
import SimulationControlPage from '../pages/SimulationControlPage';
import AlertsPage from '../pages/AlertsPage';
import ReportsPage from '../pages/ReportsPage';
import SettingsPage from '../pages/SettingsPage';

/**
 * Top-level routing. Unauthenticated users see the login screen. Authenticated users get
 * the app shell wrapped in a single shared STOMP connection (StompProvider).
 */
export default function AppRoutes() {
  const { user, loading } = useAuth();

  if (loading) {
    return <div className="centered muted">Loading…</div>;
  }

  if (!user) {
    return <LoginPage />;
  }

  return (
    <StompProvider>
      <AppLayout>
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/products" element={<ProductsPage />} />
          <Route path="/quality" element={<QualityPage />} />
          <Route path="/simulation" element={<SimulationControlPage />} />
          <Route path="/alerts" element={<AlertsPage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AppLayout>
    </StompProvider>
  );
}
