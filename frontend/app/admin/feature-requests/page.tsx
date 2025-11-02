"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { FeatureRequestsManagement } from "@/components/admin/feature-requests-management"

export default function FeatureRequestsPage() {
  return (
    <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">Feature Requests</h1>
              <p className="text-muted-foreground">Manage and prioritize feature requests from users</p>
            </div>
            <FeatureRequestsManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
