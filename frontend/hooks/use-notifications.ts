"use client"

import { useEffect, useState } from "react"
import { api, Notification } from "@/lib/api"
import { connectNotifications, disconnectNotifications } from "@/lib/notifications"
import { useAuth } from "@/components/providers/auth-provider"

export function useNotifications() {
  const { user, isAuthenticated } = useAuth()
  const [unreadCount, setUnreadCount] = useState(0)
  const [notifications, setNotifications] = useState<Notification[]>([])

  useEffect(() => {
    if (!isAuthenticated || !user) {
      setUnreadCount(0)
      setNotifications([])
      disconnectNotifications()
      return
    }

    let isMounted = true

    api
      .getUnreadNotificationCount()
      .then((res) => {
        if (res.success && isMounted) {
          setUnreadCount(res.data)
        }
      })
      .catch((err) => console.error("Failed to fetch unread notifications", err))

    connectNotifications(user.id, (notification) => {
      setNotifications((prev) => [notification, ...prev])
      setUnreadCount((prev) => prev + 1)
    })

    return () => {
      isMounted = false
      disconnectNotifications()
    }
  }, [isAuthenticated, user?.id])

  return { unreadCount, notifications, setUnreadCount }
}
