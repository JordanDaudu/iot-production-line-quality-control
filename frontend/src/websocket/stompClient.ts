import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { STOMP_ENDPOINT } from './eventTypes';

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL ?? '';

export function createStompClient(): Client {
  return new Client({
    webSocketFactory: () => new SockJS(`${WS_BASE_URL}${STOMP_ENDPOINT}`) as WebSocket,
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
  });
}
