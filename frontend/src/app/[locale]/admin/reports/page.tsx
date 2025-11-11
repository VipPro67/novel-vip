"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { ReportsManagement } from "@/components/admin/reports-management"

export default function ReportsPage() {
  return (
    <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">Reports Management</h1>
              <p className="text-muted-foreground">Handle content and user reports</p>
            </div>
            <ReportsManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
