"use client"

import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { RoleApprovalManagement } from "@/components/admin/role-approval-management"
import { Button } from "@/components/ui/button"
import { ArrowLeft } from "lucide-react"
import Link from "next/link"

export default function AdminRoleApprovalsPage() {
  return (
    <AuthGuard requireAdmin>
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
                <h1 className="text-3xl font-bold">Role Approvals</h1>
                <p className="text-muted-foreground">Manage user role requests and approvals</p>
              </div>
            </div>

            {/* Role Approval Management Component */}
            <RoleApprovalManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
