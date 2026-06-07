import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { STOMP_ENDPOINT } from './eventTypes';

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL ?? '';

export function createStompClient(): Client {
  return new Client({
    // Prefer native WebSocket, fall back to xhr-polling (plain HTTP, proxy/mobile friendly).
    // The eventsource/streaming transports are intentionally excluded: their text/event-stream
    // responses cannot carry the backend's JSON error bodies, which caused 1011 disconnects.
    webSocketFactory: () =>
      new SockJS(`${WS_BASE_URL}${STOMP_ENDPOINT}`, null, {
        transports: ['websocket', 'xhr-polling'],
      }) as WebSocket,
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
  });
}
