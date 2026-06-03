import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { StompProvider } from '../context/StompContext';
import AppLayout from '../components/layout/AppLayout';
import LoginPage from '../pages/LoginPage';
import DashboardPage from '../pages/DashboardPage';
import SimulationControlPage from '../pages/SimulationControlPage';
import PlaceholderPage from '../pages/PlaceholderPage';

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
          <Route path="/simulation" element={<SimulationControlPage />} />
          <Route path="/alerts" element={<PlaceholderPage title="Alerts" />} />
          <Route path="/reports" element={<PlaceholderPage title="Reports" />} />
          <Route path="/settings" element={<PlaceholderPage title="Settings" />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AppLayout>
    </StompProvider>
  );
}
