"use client"

import { AuthGuard } from "@/components/auth/auth-guard"
import { VideoManagement } from "@/components/admin/video-management"
import { useTranslations } from "next-intl"

export default function AdminVideosPage() {
  const t = useTranslations("AdminVideos")
  return (
    <AuthGuard requireRole={["ADMIN","AUTHOR"]}>
      <div className="min-h-screen bg-background">
        <main className="container mx-auto px-4 py-8">
          <div className="space-y-8">
            <div>
              <h1 className="text-3xl font-bold">{t("title")}</h1>
              <p className="text-muted-foreground">{t("subtitle")}</p>
            </div>
            <VideoManagement />
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
