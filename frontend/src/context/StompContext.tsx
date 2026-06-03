import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { Client, type IMessage, type StompSubscription } from '@stomp/stompjs';
import { createStompClient } from '../websocket/stompClient';

export type StompStatus = 'connecting' | 'connected' | 'disconnected';

type MessageHandler = (message: IMessage) => void;

interface Subscription {
  destination: string;
  handler: MessageHandler;
  sub?: StompSubscription;
}

interface StompContextValue {
  status: StompStatus;
  subscribe: (destination: string, handler: MessageHandler) => () => void;
}

const StompContext = createContext<StompContextValue | undefined>(undefined);

/**
 * Provides a single shared STOMP connection for the whole authenticated app. Components
 * subscribe via the returned `subscribe` function (or the useSubscription hook); all
 * subscriptions are re-established automatically if the socket reconnects.
 */
export function StompProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<StompStatus>('connecting');
  const clientRef = useRef<Client | null>(null);
  const subscriptionsRef = useRef<Map<number, Subscription>>(new Map());
  const nextIdRef = useRef(0);

  useEffect(() => {
    const client = createStompClient();
    clientRef.current = client;

    client.onConnect = () => {
      setStatus('connected');
      // (Re)subscribe everything that registered while disconnected.
      subscriptionsRef.current.forEach((entry) => {
        entry.sub = client.subscribe(entry.destination, entry.handler);
      });
    };
    client.onWebSocketClose = () => setStatus('disconnected');
    client.onStompError = () => setStatus('disconnected');

    setStatus('connecting');
    client.activate();

    return () => {
      subscriptionsRef.current.clear();
      void client.deactivate();
    };
  }, []);

  const subscribe = useCallback((destination: string, handler: MessageHandler) => {
    const id = nextIdRef.current++;
    const entry: Subscription = { destination, handler };
    subscriptionsRef.current.set(id, entry);

    const client = clientRef.current;
    if (client && client.connected) {
      entry.sub = client.subscribe(destination, handler);
    }

    return () => {
      const existing = subscriptionsRef.current.get(id);
      if (existing?.sub) {
        try {
          existing.sub.unsubscribe();
        } catch {
          // Connection may already be gone; ignore.
        }
      }
      subscriptionsRef.current.delete(id);
    };
  }, []);

  return <StompContext.Provider value={{ status, subscribe }}>{children}</StompContext.Provider>;
}

export function useStompContext(): StompContextValue {
  const ctx = useContext(StompContext);
  if (!ctx) {
    throw new Error('useStompContext must be used within a StompProvider');
  }
  return ctx;
}
