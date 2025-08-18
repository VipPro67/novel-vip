"use client"

import { Bell } from "lucide-react"
import { Button } from "@/components/ui/button"
import { useNotifications } from "@/hooks/use-notifications"
import Link from "next/link"

export function NotificationBell() {
  const { unreadCount } = useNotifications()

  return (
    <Button variant="ghost" size="icon" className="relative" asChild>
      <Link href="/notifications">
        <Bell className="h-5 w-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-1 -right-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-500 text-[10px] text-white">
            {unreadCount}
          </span>
        )}
      </Link>
    </Button>
  )
}
