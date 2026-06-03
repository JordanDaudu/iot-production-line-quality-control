import { useStompContext } from '../../context/StompContext';

/**
 * Pulsing LIVE indicator tied to the real STOMP connection state.
 */
export default function LiveBeacon() {
  const { status } = useStompContext();
  const on = status === 'connected';
  const label = on ? 'LIVE' : status === 'connecting' ? 'SYNC' : 'OFFLINE';
  return (
    <span className={`live-beacon ${on ? 'on' : 'off'}`}>
      <span className="beacon-dot" />
      {label}
    </span>
  );
}
