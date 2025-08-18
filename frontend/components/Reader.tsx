"use client"

import { PropsWithChildren, useEffect, useRef } from "react"
import { Client } from "@stomp/stompjs"
import { api } from "@/lib/api"

interface ReaderProps {
  userId: string
  novelId: string
  chapterId: string
}

export default function Reader({
  userId,
  novelId,
  chapterId,
  children,
}: PropsWithChildren<ReaderProps>) {
  const containerRef = useRef<HTMLDivElement>(null)
  const lastEmitRef = useRef(0)

  useEffect(() => {
    const wsUrl =
      (process.env.NEXT_PUBLIC_WS_URL || "ws://localhost:8081/ws").replace(
        /^http/,
        "ws",
      )
    const client = new Client({ brokerURL: wsUrl, reconnectDelay: 5000 })

    client.onConnect = () => {
      client.subscribe(`/topic/user.${userId}`, (message) => {
        try {
          const data = JSON.parse(message.body)
          if (data.novelId === novelId && data.chapterId === chapterId) {
            const el = containerRef.current
            if (el) {
              const max = el.scrollHeight - el.clientHeight
              el.scrollTop = (max * (data.progress || 0)) / 100
            }
          }
        } catch {
          // ignore
        }
      })
    }

    client.activate()
    return () => client.deactivate()
  }, [novelId, chapterId, userId])

  const emitProgress = (progress: number) => {
    const now = Date.now()
    if (now - lastEmitRef.current < 2000) return
    lastEmitRef.current = now
    api.updateReadingProgress(novelId, chapterId, progress)
  }

  const handleScroll = (e: React.UIEvent<HTMLDivElement>) => {
    const target = e.currentTarget
    const max = target.scrollHeight - target.clientHeight
    if (max <= 0) return
    const pct = Math.round((target.scrollTop / max) * 100)
    emitProgress(pct)
  }

  return (
    <div
      onScroll={handleScroll}
      className="reader"
      id="reader-container"
      ref={containerRef}
    >
      {children}
    </div>
  )
}
