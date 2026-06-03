import axios from 'axios';

// Shared Axios instance. All REST calls go through /api (proxied to the backend in dev).
// HTTP Basic credentials are attached on every request once the user logs in.

const STORAGE_KEY = 'smartiot.basicAuth';
let basicAuth: string | null = sessionStorage.getItem(STORAGE_KEY);

export const httpClient = axios.create({
  baseURL: '/api',
});

httpClient.interceptors.request.use((config) => {
  if (basicAuth) {
    config.headers.Authorization = `Basic ${basicAuth}`;
  }
  return config;
});

export function setCredentials(username: string, password: string): void {
  basicAuth = btoa(`${username}:${password}`);
  sessionStorage.setItem(STORAGE_KEY, basicAuth);
}

export function clearCredentials(): void {
  basicAuth = null;
  sessionStorage.removeItem(STORAGE_KEY);
}

export function hasCredentials(): boolean {
  return basicAuth !== null;
}
