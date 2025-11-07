"use client"

import { useEffect, useState, useCallback } from "react"
import { api, Notification } from "@/lib/api"
import { connectNotifications, disconnectNotifications } from "@/lib/notifications"
import { useAuth } from "@/components/providers/auth-provider"

export function useNotifications() {
  const { user, isAuthenticated } = useAuth()
  const [unreadCount, setUnreadCount] = useState(0)
  const [notifications, setNotifications] = useState<Notification[]>([])

  const refreshUnreadCount = useCallback(() => {
    api.getUnreadNotificationCount()
      .then((res) => {
        if (res.success) setUnreadCount(res.data)
      })
      .catch((err) => console.error("Failed to fetch unread notifications", err))
  }, [])

  useEffect(() => {
    if (!isAuthenticated || !user) {
      setUnreadCount(0)
      setNotifications([])
      disconnectNotifications()
      return
    }

    let isMounted = true

    // 🔹 Fetch unread count immediately on login/page load
    refreshUnreadCount()

    // 🔹 Connect websocket for live notifications
    connectNotifications(user.id, (notification) => {
      if (!isMounted) return
      setNotifications((prev) => [notification, ...prev])
      if (!notification.read) {
        setUnreadCount((prev) => prev + 1)
      }
    })

    return () => {
      isMounted = false
      disconnectNotifications()
    }
  }, [isAuthenticated, user?.id, refreshUnreadCount])

  return { unreadCount, notifications, setNotifications, refreshUnreadCount, setUnreadCount }
}
