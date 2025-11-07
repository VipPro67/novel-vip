"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { NotificationCenter } from "@/components/admin/notification-center"

export default function NotificationsPage() {
  return (
    <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">Notification Center</h1>
              <p className="text-muted-foreground">Create and manage system-wide notifications</p>
            </div>
            <NotificationCenter />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
