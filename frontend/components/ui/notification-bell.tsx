"use client"

import { useEffect, useState } from "react"
import { Bell } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
	Popover,
	PopoverContent,
	PopoverTrigger,
} from "@/components/ui/popover"
import { useNotifications } from "@/hooks/use-notifications"
import { api, Notification } from "@/lib/api"
import clsx from "clsx"

export function NotificationBell() {
	const {
		unreadCount,
		notifications,
		setNotifications,
		refreshUnreadCount,
		setUnreadCount,
	} = useNotifications()

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

	// Mark all as read
	const markAllRead = () => {
		api.markAllNotificationsAsRead().then(() => {
			setUnreadCount(0)
			setNotifications((prev) => prev.map((n) => ({ ...n, read: true })))
		})
	}

	// Mark single notification as read
	const markOneRead = (id: string) => {
		api.markNotificationAsRead(id).then(() => {
			setNotifications((prev) =>
				prev.map((n) => (n.id === id ? { ...n, read: true } : n))
			)
			refreshUnreadCount()
		})
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
