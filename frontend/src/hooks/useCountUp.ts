import { useEffect, useRef, useState } from 'react';

/**
 * Animates a number from its previous value to the new one (count-up effect).
 */
export function useCountUp(value: number, duration = 600): number {
  const [display, setDisplay] = useState(value);
  const fromRef = useRef(value);

  useEffect(() => {
    const from = fromRef.current;
    const to = value;
    if (from === to) {
      return;
    }
    const start = performance.now();
    let raf = 0;
    const ease = (p: number) => 1 - Math.pow(1 - p, 3);
    const tick = (now: number) => {
      const p = Math.min(1, (now - start) / duration);
      setDisplay(Math.round(from + (to - from) * ease(p)));
      if (p < 1) {
        raf = requestAnimationFrame(tick);
      } else {
        fromRef.current = to;
      }
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [value, duration]);

  return display;
}
