import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// During development the Vite dev server (port 5173) proxies API and WebSocket traffic
// to the Spring Boot backend (port 8080). This keeps everything same-origin, so no CORS
// handling is needed in the browser and the STOMP/SockJS handshake works through /ws.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
    },
  },
});
