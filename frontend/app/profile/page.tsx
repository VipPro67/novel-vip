"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { Header } from "@/components/layout/header"
import { MyProfile } from "@/components/user/my-profile"
import { RoleRequestForm } from "@/components/user/role-request-form"
import { MyRoleRequests } from "@/components/user/my-role-requests"
import { useState } from "react"

export default function ProfilePage() {
  return (
    <AuthGuard>
      <ProfileContent />
    </AuthGuard>
  )
}

function ProfileContent() {
  const [refreshRequests, setRefreshRequests] = useState(0)

  const handleRequestSubmitted = () => {
    setRefreshRequests((prev) => prev + 1)
  }

  return (
    <div className="min-h-screen bg-background">
      <main className="container py-8">
        <div className="max-w-6xl mx-auto space-y-8">
          <div>
            <h1 className="text-3xl font-bold">My Profile</h1>
            <p className="text-muted-foreground">Manage your profile information and role requests</p>
          </div>

          <div className="space-y-6">
            <MyProfile />

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <RoleRequestForm onRequestSubmitted={handleRequestSubmitted} />
              <div key={refreshRequests}>
                <MyRoleRequests />
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
