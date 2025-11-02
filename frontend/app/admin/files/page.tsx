"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { FileManagement } from "@/components/admin/file-management"

export default function FilesPage() {
  return (
    <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">File Management</h1>
              <p className="text-muted-foreground">Manage uploaded files in the system</p>
            </div>
            <FileManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
