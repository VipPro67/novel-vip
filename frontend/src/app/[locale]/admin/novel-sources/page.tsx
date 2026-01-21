"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { NovelSourcesManagement } from "@/components/admin/novel-sources-management"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import { Link } from "@/navigation"

export default function AdminNovelSourcesPage() {
  return (
    <AuthGuard requireRole={["ADMIN", "AUTHOR"]}>
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
              <Link href="/admin">
                <Button variant="outline" size="sm">
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back to Dashboard
                </Button>
              </Link>
              <div>
                <h1 className="text-3xl font-bold">Novel Sources Management</h1>
                <p className="text-muted-foreground">
                  Manage automatic chapter imports from external sources
                </p>
              </div>
            </div>

            {/* Management Component */}
            <NovelSourcesManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
