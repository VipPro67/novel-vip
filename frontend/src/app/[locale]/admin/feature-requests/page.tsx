"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { FeatureRequestsManagement } from "@/components/admin/feature-requests-management"
import { useTranslations } from "next-intl"

export default function FeatureRequestsPage() {
  const t = useTranslations("Admin")
  return (
    <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">{t("featureRequests.title")}</h1>
              <p className="text-muted-foreground">{t("featureRequests.subtitle")}</p>
            </div>
            <FeatureRequestsManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
