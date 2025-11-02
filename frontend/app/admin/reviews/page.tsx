"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { ReviewsManagement } from "@/components/admin/reviews-management"

export default function ReviewsPage() {
  return (
    <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">Reviews Management</h1>
              <p className="text-muted-foreground">Moderate novel reviews and ratings</p>
            </div>
            <ReviewsManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
