"use client";

import { useEffect, useMemo, useRef, useState } from "react";

export function useSuggest() {
  const [value, setValue] = useState("");
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const abortRef = useRef<AbortController | null>(null);

  const setQueryDebounced = useMemo(() => {
    let t: any;
    return (v: string) => {
      clearTimeout(t);
      t = setTimeout(() => fetchSuggest(v), 250);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function fetchSuggest(q: string) {
    if (!q.trim()) {
      setItems([]); setOpen(false); return;
    }
    abortRef.current?.abort();
    const ac = new AbortController();
    abortRef.current = ac;
    setLoading(true);
	console.log(process.env.NEXT_PUBLIC_API_BASE);
    fetch(`${process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8081"}/api/novels/suggest?q=${encodeURIComponent(q)}&limit=8`, {
      signal: ac.signal,
      cache: "no-store"
    })
      .then(r => r.ok ? r.json() : Promise.reject(r))
      .then((list: string[]) => { setItems(list); setOpen(true); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }

  return {
    value, setValue,
    open, setOpen,
    items, loading,
    setQueryDebounced
  };
}
