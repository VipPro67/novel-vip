'use client';

import { useEffect, useRef, useState } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import { Input } from '@/components/ui/input';

/**
 * Search bar component that communicates with the backend over a WebSocket
 * connection. Queries are debounced before being sent and results are rendered
 * in a dropdown below the input.
 */
export default function SearchBar() {
  const [query, setQuery] = useState('');
  const [suggestions, setSuggestions] = useState<string[]>([]);
  const clientRef = useRef<Client | null>(null);
  const timeout = useRef<NodeJS.Timeout>();

  useEffect(() => {
    const client = new Client({
      brokerURL: process.env.NEXT_PUBLIC_BACKEND_WS || 'ws://localhost:8081/ws',
    });

    client.onConnect = () => {
      client.subscribe('/topic/search', (message: IMessage) => {
        try {
          const data = JSON.parse(message.body) as any[];
          if (Array.isArray(data)) {
            const titles = data.map((item) => (typeof item === 'string' ? item : item.title));
            setSuggestions(titles);
          }
        } catch {
          setSuggestions([]);
        }
      });
    };

    client.activate();
    clientRef.current = client;
    return () => client.deactivate();
  }, []);

  const sendQuery = (q: string) => {
    const client = clientRef.current;
    if (client && client.connected) {
      client.publish({ destination: '/app/search', body: q });
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setQuery(value);
    clearTimeout(timeout.current);
    timeout.current = setTimeout(() => sendQuery(value), 300);
  };

  return (
    <div className="relative w-full">
      <Input value={query} onChange={handleChange} placeholder="Search novels..." />
      {suggestions.length > 0 && (
        <ul className="absolute z-10 mt-1 w-full rounded-md border bg-background shadow">
          {suggestions.map((s, i) => (
            <li
              key={i}
              className="cursor-pointer px-3 py-2 hover:bg-accent"
              onMouseDown={() => setQuery(s)}
            >
              {s}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
