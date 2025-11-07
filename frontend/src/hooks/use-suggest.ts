"use client";

import { useEffect, useMemo, useRef, useState } from "react";

export type SuggestItem = { id: string; title: string };

export function useSuggest() {
  const [value, setValue] = useState("");
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<SuggestItem[]>([]);
  const [loading, setLoading] = useState(false);
  const abortRef = useRef<AbortController | null>(null);

  const setQueryDebounced = useMemo(() => {
    let t: any;
    return (v: string) => {
      clearTimeout(t);
      t = setTimeout(() => fetchSuggest(v), 250);
    };
  }, []);

  function fetchSuggest(q: string) {
    if (!q.trim()) {
      setItems([]); setOpen(false); return;
    }
    abortRef.current?.abort();
    const ac = new AbortController();
    abortRef.current = ac;
    setLoading(true);

    const base = process.env.NEXT_PUBLIC_API_BASE ?? "http://localhost:8081";
    fetch(`${base}/api/novels/suggest?q=${encodeURIComponent(q)}&size=8`, {
      signal: ac.signal,
      cache: "no-store",
    })
      .then((r) => (r.ok ? r.json() : Promise.reject(r)))
      .then((list: any) => {
        const parsed: SuggestItem[] = Array.isArray(list)
          ? list.map((x: any) =>
              typeof x === "string" ? ({ id: "", title: x }) : ({ id: x.id, title: x.title })
            ).filter(x => x.title && typeof x.title === "string")
          : [];
        setItems(parsed);
        setOpen(parsed.length > 0);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }

  return { value, setValue, open, setOpen, items, loading, setQueryDebounced };
}
