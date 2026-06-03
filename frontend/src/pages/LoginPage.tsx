import { useState, type FormEvent } from 'react';
import { useAuth } from '../context/AuthContext';

const DEMO_USERS = [
  { username: 'admin', password: 'admin123', role: 'System Administrator' },
  { username: 'manager', password: 'manager123', role: 'Quality Manager' },
  { username: 'operator', password: 'operator123', role: 'Production Line Operator' },
  { username: 'tech', password: 'tech123', role: 'Maintenance Technician' },
];

export default function LoginPage() {
  const { login } = useAuth();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('admin123');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(username, password);
    } catch {
      setError('Invalid username or password.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="login-screen">
      <form className="card login-card" onSubmit={handleSubmit}>
        <h1>Smart IoT Quality Inspection</h1>
        <p className="muted">Sign in to monitor the production line.</p>

        <label>
          Username
          <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
        </label>
        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </label>

        {error && <div className="error-text">{error}</div>}

        <button type="submit" disabled={submitting}>
          {submitting ? 'Signing in…' : 'Sign in'}
        </button>

        <div className="demo-users">
          <span className="muted">Demo accounts (password = username + 123):</span>
          <ul>
            {DEMO_USERS.map((u) => (
              <li key={u.username}>
                <button
                  type="button"
                  className="link"
                  onClick={() => {
                    setUsername(u.username);
                    setPassword(u.password);
                  }}
                >
                  {u.username}
                </button>
                <span className="muted"> — {u.role}</span>
              </li>
            ))}
          </ul>
        </div>
      </form>
    </div>
  );
}
