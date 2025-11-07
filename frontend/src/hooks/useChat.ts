"use client";

import { useEffect, useState } from "react";

export function useChat(room: string) {
  const [messages, setMessages] = useState<any[]>([]);

  useEffect(() => {
    const url = process.env.NEXT_PUBLIC_WS_URL || "ws://localhost:8081/ws";
    const ws = new WebSocket(url);

    ws.onopen = () => {
      ws.send("CONNECT\n\n\u0000");
      ws.send(`SUBSCRIBE\nid:sub-0\ndestination:${room}\n\n\u0000`);
    };

    ws.onmessage = (event) => {
      const frame = event.data as string;
      const idx = frame.indexOf("\n\n");
      if (idx !== -1) {
        const body = frame.substring(idx + 2, frame.length - 1);
        try {
          setMessages((prev) => [...prev, JSON.parse(body)]);
        } catch {
          // ignore non JSON messages
        }
      }
    };

    return () => {
      ws.send("DISCONNECT\n\n\u0000");
      ws.close();
    };
  }, [room]);

  return { messages };
}
