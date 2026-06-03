import { createContext, useContext, useEffect, useState, type ReactNode } from 'react';
import { httpClient, setCredentials, clearCredentials, hasCredentials } from '../api/httpClient';
import type { CurrentUser } from '../types/auth';

interface AuthContextValue {
  user: CurrentUser | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

/**
 * Holds the authenticated user. On mount, if credentials are already stored (page
 * refresh), it revalidates them against GET /api/auth/me.
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!hasCredentials()) {
      setLoading(false);
      return;
    }
    httpClient
      .get<CurrentUser>('/auth/me')
      .then((res) => setUser(res.data))
      .catch(() => clearCredentials())
      .finally(() => setLoading(false));
  }, []);

  async function login(username: string, password: string): Promise<void> {
    setCredentials(username, password);
    try {
      const res = await httpClient.get<CurrentUser>('/auth/me');
      setUser(res.data);
    } catch (error) {
      clearCredentials();
      throw error;
    }
  }

  function logout(): void {
    clearCredentials();
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
