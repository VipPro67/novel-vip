"use client"

import { useEffect, useState } from "react"
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

  useEffect(() => {
    if (isAuthenticated) {
      api.getReadingHistory(
        {
          page: 0,
          size: 6,
          sortBy: "updatedAt",
          sortDir: "desc",
        }
      )
        .then((res) => {
           if (res.success) 
            {
                setRecentlyRead(res.data.content)
            }
        })
        .finally(() => setLoading(false))
    } else {
        setLoading(false)
    }
  }, [isAuthenticated])

  if (!isAuthenticated || (!loading && recentlyRead.length === 0)) {
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
        // Skeleton loading cho riêng phần này
        <div className="grid grid-cols-1 md:grid-cols-6 gap-6">
             {/* ... skeleton code ... */}
             <div className="h-48 bg-muted animate-pulse rounded-md"></div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-6">
          {Array.isArray(recentlyRead) && recentlyRead.length > 0 ? (
            recentlyRead.map((history) => (
            <ReadingHistoryCard key={history.id} history={history} />
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