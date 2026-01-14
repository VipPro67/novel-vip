"use client"

import { useEffect, useState, useCallback } from "react"
import { api } from "@/services/api"
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
  }, []) // Empty deps - function doesn't depend on anything

  useEffect(() => {
    if (!isAuthenticated || !user) {
      setUnreadCount(0)
      setNotifications([])
      disconnectNotifications()
      return
    }

    console.log("[useNotifications] Effect triggered for user:", user.id)
    let isMounted = true
    let connectionTimeout: NodeJS.Timeout

    // 🔹 Fetch unread count immediately on login/page load
    refreshUnreadCount()

    // 🔹 Debounce SSE connection to prevent duplicate connections during rapid remounts
    connectionTimeout = setTimeout(() => {
      if (!isMounted) {
        console.log("[useNotifications] Component unmounted before connection, skipping...")
        return
      }
      
      console.log("[useNotifications] Attempting to connect SSE for user:", user.id)
      // Connect SSE for live notifications
      const connected = connectNotifications(user.id, (notification) => {
        if (!isMounted) return
        setNotifications((prev) => [notification, ...prev])
        if (!notification.read) {
          setUnreadCount((prev) => prev + 1)
        }
      })
      console.log("[useNotifications] Connection attempt result:", connected)
    }, 100) // Small delay to debounce rapid remounts

    return () => {
      console.log("[useNotifications] Cleanup: disconnecting for user:", user.id)
      isMounted = false
      clearTimeout(connectionTimeout)
      disconnectNotifications()
    }
  }, [isAuthenticated, user?.id]) // Removed refreshUnreadCount from deps

  return { unreadCount, notifications, setNotifications, refreshUnreadCount, setUnreadCount }
}
