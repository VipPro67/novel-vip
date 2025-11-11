"use client"

import { useEffect, useState } from "react"
import { useRouter } from "@/navigation"
import { Bell } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover"
import { useNotifications } from "@/hooks/use-notifications"
import clsx from "clsx"
import { api } from "@/services/api"

export function NotificationBell() {
    const {
        unreadCount,
        notifications,
        setNotifications,
        refreshUnreadCount,
        setUnreadCount,
    } = useNotifications()

    const router = useRouter()

    const [open, setOpen] = useState(false)

    // Fetch notifications when opened
    useEffect(() => {
        if (!open) return
        api.getNotifications({ page: 0, size: 10 }).then((res) => {
            if (res.success) {
                setNotifications(res.data.content)
            }
        })
    }, [open, setNotifications])

    const markAllRead = () => {
        api.markAllNotificationsAsRead().then(() => {
            setUnreadCount(0)
            setNotifications((prev) => prev.map((n) => ({ ...n, read: true })))
        })
    }

    const markOneRead = async (id: string) => {
        const notification = notifications.find((n) => n.id === id)
        if (!notification) return

        // Mark as read on server first (if unread)
        if (notification.read === false) {
            try {
                await api.markNotificationAsRead(id)
                setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, read: true } : n)))
                refreshUnreadCount()
            } catch (err) {
                console.error("Failed to mark notification as read", err)
            }
        }

        // Use Next.js client navigation instead of forcing full page reload
        if (notification.reference) {
            switch (notification.type) {
                case "CHAPTER_UPDATE":
                    // use the same URL shape used elsewhere (`/novels/[slug]`)
                    await router.push(`/novels/${notification.reference}`)
                    break
                case "COMMENT":
                    await router.push(`/comments/${notification.reference}`)
                    break
                case "LIKE":
                    await router.push(`/likes/${notification.reference}`)
                    break
                case "FOLLOW":
                    await router.push(`/user/${notification.reference}`)
                    break
                case "MESSAGE":
                    await router.push(`/messages/${notification.reference}`)
                    break
                default:
                    break
            }
        }
    }

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
            <PopoverContent className="w-96 p-0" align="end">
                <div className="flex items-center justify-between p-2 border-b">
                    <span className="font-medium text-sm">Notifications</span>
                    {notifications.length > 0 && unreadCount > 0 && (
                        <Button variant="ghost" size="sm" onClick={markAllRead}>
                            Mark all read
                        </Button>
                    )}
                </div>

                <div className="max-h-72 overflow-y-auto">
                    {notifications.length === 0 && (
                        <p className="p-4 text-sm text-muted-foreground">
                            No notifications
                        </p>
                    )}
                    {notifications.map((n) => (
                        <div
                            key={n.id}
                            className={clsx(
                                "cursor-pointer border-b p-4 last:border-b-0",
                                !n.read && "bg-muted"
                            )}
                            onClick={() => markOneRead(n.id)}
                        >
                            <p className={clsx("text-sm", !n.read && "font-semibold")}>
                                {n.title}
                            </p>
                            <p className="text-xs text-muted-foreground">{n.message}</p>
                        </div>
                    ))}
                </div>
            </PopoverContent>
        </Popover>
    )
}
