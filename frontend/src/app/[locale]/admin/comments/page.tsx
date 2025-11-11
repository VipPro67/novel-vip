"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { CommentsManagement } from "@/components/admin/comments-management"

export default function CommentsPage() {
  return (
    <AuthGuard requireRole={["ADMIN","MODERATOR"]}>
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">Comments Management</h1>
              <p className="text-muted-foreground">Moderate and manage all user comments</p>
            </div>
            <CommentsManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
