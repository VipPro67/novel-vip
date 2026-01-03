"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { CorrectionsManagement } from "@/components/admin/corrections-management"

export default function CorrectionsPage() {
  return (
    <AuthGuard requireRole={["ADMIN", "MODERATOR"]}>
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">Corrections Management</h1>
              <p className="text-muted-foreground">Review and manage text correction requests</p>
            </div>
            <CorrectionsManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}