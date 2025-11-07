"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { VideoManagement } from "@/components/admin/video-management"

export default function AdminVideosPage() {
  return (
    <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">Video Library</h1>
              <p className="text-muted-foreground">Manage videos embedded across the platform.</p>
            </div>
            <VideoManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
