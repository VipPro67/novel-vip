"use client"

import { useEffect, useState, useCallback } from "react"
import { useTranslations } from "next-intl"
import { ArrowRight, Clock } from "lucide-react"
import { Link } from "@/navigation"
import { Button } from "@/components/ui/button"
import { ReadingHistoryCard } from "@/components/novel/reading-history-card"
import { useAuth } from "@/components/providers/auth-provider"
import { api } from "@/services/api"
import { ReadingHistory } from "@/models"

function RecentlyReadSkeleton() {
  return (
    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
      {Array.from({ length: 4 }).map((_, i) => (
        <div key={i} className="space-y-3">
          <div className="aspect-[3/4] bg-muted animate-pulse rounded" />
          <div className="h-4 bg-muted animate-pulse rounded" />
          <div className="h-3 w-3/4 bg-muted animate-pulse rounded" />
        </div>
      ))}
    </div>
  )
}

export function RecentlyReadSection() {
  const t = useTranslations("Home")
  const { isAuthenticated, loading: authLoading } = useAuth()
  const [recentlyRead, setRecentlyRead] = useState<ReadingHistory[]>([])
  const [loading, setLoading] = useState(true)
  const [initialized, setInitialized] = useState(false)

  const fetchHistory = useCallback(() => {
    if (!isAuthenticated) {
      setLoading(false)
      setInitialized(true)
      return
    }

    api.getReadingHistory({
      page: 0,
      size: 4,
      sortBy: "updatedAt",
      sortDir: "desc",
    })
      .then((res) => {
        if (res.success) {
          setRecentlyRead(res.data.content)
        }
      })
      .finally(() => {
        setLoading(false)
        setInitialized(true)
      })
  }, [isAuthenticated])

  useEffect(() => {
    // Wait for auth to be determined before fetching
    if (!authLoading) {
      fetchHistory()
    }
  }, [fetchHistory, authLoading])

  if (authLoading) {
    return (
      <section className="min-h-[280px]" aria-hidden="true">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <div className="h-6 w-6 bg-muted animate-pulse rounded" />
            <div className="h-8 w-48 bg-muted animate-pulse rounded" />
          </div>
          <div className="h-10 w-24 bg-muted animate-pulse rounded" />
        </div>
        <RecentlyReadSkeleton />
      </section>
    )
  }

  if (!isAuthenticated) {
    return null
  }

  if (loading) {
    return (
      <section className="min-h-[280px]">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <Clock className="h-6 w-6 text-primary" />
            <h2 className="text-3xl font-bold">{t("continueReading")}</h2>
          </div>
          <Button asChild variant="ghost">
            <Link href="/profile">
              {t("viewAll")} <ArrowRight className="ml-2 h-4 w-4" />
            </Link>
          </Button>
        </div>
        <RecentlyReadSkeleton />
      </section>
    )
  }

  if (recentlyRead.length === 0) {
    return null
  }

  return (
    <section className="min-h-[280px]">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center space-x-2">
          <Clock className="h-6 w-6 text-primary" />
          <h2 className="text-3xl font-bold">{t("continueReading")}</h2>
        </div>
        <Button asChild variant="ghost">
          <Link href="/profile">
            {t("viewAll")} <ArrowRight className="ml-2 h-4 w-4" />
          </Link>
        </Button>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
        {recentlyRead.map((history) => (
          <ReadingHistoryCard key={history.id} history={history} hideImage />
        ))}
      </div>
    </section>
  )
}