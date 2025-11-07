"use client"

import { useEffect, useState } from "react"
import { api, Notification } from "@/lib/api"
import { useAuth } from "@/components/providers/auth-provider"

export default function NotificationsPage() {
  const { isAuthenticated } = useAuth()
  const [notifications, setNotifications] = useState<Notification[]>([])

  useEffect(() => {
    if (!isAuthenticated) return

    api
      .getNotifications({ page: 0, size: 20 })
      .then((res) => {
        if (res.success) {
          setNotifications(res.data.content)
        }
      })
      .catch((err) => console.error("Failed to fetch notifications", err))
  }, [isAuthenticated])

  return (
    <div className="container mx-auto py-8">
      <h1 className="text-2xl font-bold mb-4">Notifications</h1>
      <div className="space-y-4">
        {notifications.length === 0 && <p>No notifications</p>}
        {notifications.map((n) => (
          <div key={n.id} className="rounded-md border p-4">
            <p className="font-medium">{n.title}</p>
            <p className="text-sm text-muted-foreground">{n.message}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
