"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { MessagesManagement } from "@/components/admin/messages-management"

export default function MessagesPage() {
  return (
    <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">Messages & Groups</h1>
              <p className="text-muted-foreground">Monitor user messages and group conversations</p>
            </div>
            <MessagesManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
