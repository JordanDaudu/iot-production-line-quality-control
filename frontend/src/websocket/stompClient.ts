import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { STOMP_ENDPOINT } from './eventTypes';

/**
 * Creates a STOMP client that connects over SockJS to the backend /ws endpoint.
 * In development the Vite proxy forwards /ws to the Spring Boot server. The client
 * auto-reconnects so the dashboard recovers if the backend restarts (NFR-07).
 */
export function createStompClient(): Client {
  return new Client({
    // SockJS resolves the relative URL against the current origin (proxied by Vite).
    webSocketFactory: () => new SockJS(STOMP_ENDPOINT) as WebSocket,
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
  });
}
