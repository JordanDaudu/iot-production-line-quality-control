import { useEffect, useRef } from 'react';
import type { IMessage } from '@stomp/stompjs';
import { useStompContext } from '../context/StompContext';

/**
 * Subscribes to a STOMP destination for the lifetime of the component. The handler can
 * change between renders without causing a resubscribe (the latest one is always used).
 */
export function useSubscription(destination: string, onMessage: (message: IMessage) => void) {
  const { subscribe } = useStompContext();
  const handlerRef = useRef(onMessage);
  handlerRef.current = onMessage;

  useEffect(() => {
    const unsubscribe = subscribe(destination, (message) => handlerRef.current(message));
    return unsubscribe;
  }, [destination, subscribe]);
}
