import { useRef, useState } from 'react';
import { useSubscription } from '../../hooks/useSubscription';
import { Topics } from '../../websocket/eventTypes';
import type { Alert } from '../../types/alert';

/**
 * Slide-in toast notifications for newly created alerts. Critical alerts glow. Toasts
 * auto-dismiss; at most four are shown at once.
 */
export default function Toaster() {
  const [toasts, setToasts] = useState<{ id: number; alert: Alert }[]>([]);
  const idRef = useRef(0);

  useSubscription(Topics.ALERTS, (message) => {
    const alert = JSON.parse(message.body) as Alert;
    if (alert.status !== 'ACTIVE') {
      return; // only announce newly raised alerts, not acks/resolves
    }
    const id = idRef.current++;
    setToasts((prev) => [...prev, { id, alert }].slice(-4));
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 6000);
  });

  return (
    <div className="toast-stack">
      {toasts.map((t) => (
        <div key={t.id} className={`toast sev-${t.alert.severity.toLowerCase()}`}>
          <div className="toast-head">
            <span>{t.alert.type.replace(/_/g, ' ')}</span>
            <span>{t.alert.severity}</span>
          </div>
          <div className="toast-body">{t.alert.message}</div>
        </div>
      ))}
    </div>
  );
}
