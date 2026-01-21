"use client"

import { useEffect, useState, useMemo, useCallback } from "react"
import { useTranslations } from "next-intl"
import { ArrowRight, Clock } from "lucide-react"
import { Link } from "@/navigation"
import { Button } from "@/components/ui/button"
import { ReadingHistoryCard } from "@/components/novel/reading-history-card"
import { useAuth } from "@/components/providers/auth-provider"
import { api } from "@/services/api"
import { Novel, ReadingHistory } from "@/models"

export function RecentlyReadSection() {
  const t = useTranslations("Home")
  const { isAuthenticated } = useAuth()
  const [recentlyRead, setRecentlyRead] = useState<ReadingHistory[]>([])
  const [loading, setLoading] = useState(true)

  const fetchHistory = useCallback(() => {
    if (!isAuthenticated) {
      setLoading(false)
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
      .finally(() => setLoading(false))
  }, [isAuthenticated])

  useEffect(() => {
    fetchHistory()
  }, [fetchHistory])

  // Early return - don't subscribe to state in render if not shown
  const shouldShow = useMemo(
    () => isAuthenticated && (loading || recentlyRead.length > 0),
    [isAuthenticated, loading, recentlyRead.length]
  )

  if (!shouldShow) {
    return null
  }

  return (
    <section>
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

      {loading ? (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="space-y-3">
              <div className="aspect-[3/4] bg-muted animate-pulse rounded" />
              <div className="h-4 bg-muted animate-pulse rounded" />
              <div className="h-3 w-3/4 bg-muted animate-pulse rounded" />
            </div>
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {Array.isArray(recentlyRead) && recentlyRead.length > 0 ? (
            recentlyRead.map((history) => (
            <ReadingHistoryCard key={history.id} history={history} hideImage />
            ))
        ) : (
            <div className="col-span-full text-center text-muted-foreground">
            Không có lịch sử đọc.
            </div>
        )}
        </div>
      )}
    </section>
  )
}