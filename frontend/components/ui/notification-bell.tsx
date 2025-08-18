"use client"

import { useEffect, useState } from "react"
import { Bell } from "lucide-react"
import Link from "next/link"

import { Button } from "@/components/ui/button"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { useNotifications } from "@/hooks/use-notifications"
import { api, Notification } from "@/lib/api"

export function NotificationBell() {
  const { unreadCount, notifications: liveNotifications, setUnreadCount } =
    useNotifications()
  const [open, setOpen] = useState(false)
  const [notifications, setNotifications] = useState<Notification[]>([])

  // Fetch notifications when popover is opened
  useEffect(() => {
    if (!open) return

    api
      .getNotifications({ page: 0, size: 20 })
      .then((res) => {
        if (res.success) {
          setNotifications(res.data.content)
        }
      })
      .catch((err) => console.error("Failed to fetch notifications", err))

    api
      .markAllNotificationsAsRead()
      .then(() => setUnreadCount(0))
      .catch((err) =>
        console.error("Failed to mark notifications as read", err)
      )
  }, [open, setUnreadCount])

  // Merge live notifications from websocket
  useEffect(() => {
    if (liveNotifications.length === 0) return
    setNotifications((prev) => {
      const existing = new Set(prev.map((n) => n.id))
      const newOnes = liveNotifications.filter((n) => !existing.has(n.id))
      return [...newOnes, ...prev]
    })
  }, [liveNotifications])

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="ghost" size="icon" className="relative">
          <Bell className="h-5 w-5" />
          {unreadCount > 0 && (
            <span className="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] text-white">
              {unreadCount}
            </span>
          )}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-80 p-0" align="end">
        <div className="max-h-60 overflow-y-auto">
          {notifications.length === 0 && (
            <p className="p-4 text-sm text-muted-foreground">No notifications</p>
          )}
          {notifications.map((n) => (
            <div key={n.id} className="border-b p-4 last:border-b-0">
              <p className="font-medium">{n.title}</p>
              <p className="text-sm text-muted-foreground">{n.message}</p>
            </div>
          ))}
        </div>
        <Button variant="ghost" className="w-full" asChild>
          <Link href="/notifications">View all</Link>
        </Button>
      </PopoverContent>
    </Popover>
  )
}
