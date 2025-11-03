"use client"

import { useEffect, useState } from "react"
import { Grid, List, Clock, Calendar, Zap, BookOpen } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Header } from "@/components/layout/header"
import { NovelCard } from "@/components/novel/novel-card"
import { api, type Novel } from "@/lib/api"
import { Pagination } from "@/components/ui/pagination"

export default function LatestNovelsPage() {
  const [novels, setNovels] = useState<Novel[]>([])
  const [loading, setLoading] = useState(true)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid")

  useEffect(() => {
    fetchLatestNovels()
  }, [currentPage])

  const fetchLatestNovels = async () => {
    setLoading(true)
    try {
      const response = await api.getLatestNovels({
        page: currentPage,
        size: 20,
      })

      if (response.success) {
        setNovels(response.data.content)
        setTotalPages(response.data.totalPages)
      }
    } catch (error) {
      console.error("Failed to fetch latest novels:", error)
    } finally {
      setLoading(false)
    }
  }

  const getUpdateBadge = (updatedAt: string) => {
    const now = new Date()
    const updated = new Date(updatedAt)
    const diffHours = Math.floor((now.getTime() - updated.getTime()) / (1000 * 60 * 60))

    if (diffHours < 1) return <Badge className="bg-green-500 text-white">Just now</Badge>
    if (diffHours < 24) return <Badge className="bg-blue-500 text-white">{diffHours}h ago</Badge>
    if (diffHours < 168) return <Badge className="bg-purple-500 text-white">{Math.floor(diffHours / 24)}d ago</Badge>
    return <Badge className="bg-gray-500 text-white">{Math.floor(diffHours / 168)}w ago</Badge>
  }

  return (
    <div className="min-h-screen bg-background">
      <main className="container py-8">
        <div className="flex flex-col space-y-6">
          {/* Header */}
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div className="flex items-center space-x-3">
              <Clock className="h-8 w-8 text-blue-500" />
              <div>
                <h1 className="text-3xl font-bold">Latest Updates</h1>
                <p className="text-muted-foreground">Recently updated novels with fresh content</p>
              </div>
            </div>

            <div className="flex border rounded-md">
              <Button
                variant={viewMode === "grid" ? "default" : "ghost"}
                size="icon"
                onClick={() => setViewMode("grid")}
              >
                <Grid className="h-4 w-4" />
              </Button>
              <Button
                variant={viewMode === "list" ? "default" : "ghost"}
                size="icon"
                onClick={() => setViewMode("list")}
              >
                <List className="h-4 w-4" />
              </Button>
            </div>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Today</CardTitle>
                <Zap className="h-4 w-4 text-blue-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">127</div>
                <p className="text-xs text-muted-foreground">updated today</p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">This Week</CardTitle>
                <Calendar className="h-4 w-4 text-blue-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">892</div>
                <p className="text-xs text-muted-foreground">updated this week</p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Regular</CardTitle>
                <Clock className="h-4 w-4 text-blue-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">1.2K</div>
                <p className="text-xs text-muted-foreground">regular updates</p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Fresh</CardTitle>
                <BookOpen className="h-4 w-4 text-blue-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">45</div>
                <p className="text-xs text-muted-foreground">new chapters</p>
              </CardContent>
            </Card>
          </div>

          {/* Content */}
          {loading ? (
            <div
              className={`grid gap-6 ${
                viewMode === "grid"
                  ? "grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5"
                  : "grid-cols-1"
              }`}
            >
              {Array.from({ length: 20 }).map((_, i) => (
                <Card key={i} className="animate-pulse">
                  <div className="aspect-[3/4] bg-muted" />
                  <CardContent className="p-4">
                    <div className="h-4 bg-muted rounded mb-2" />
                    <div className="h-3 bg-muted rounded mb-2" />
                    <div className="h-3 bg-muted rounded" />
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : novels.length === 0 ? (
            <div className="text-center py-12">
              <Clock className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground">No recently updated novels found</p>
            </div>
          ) : (
            <div
              className={`grid gap-6 ${
                viewMode === "grid"
                  ? "grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5"
                  : "grid-cols-1"
              }`}
            >
              {novels.map((novel) => (
                <div key={novel.id} className="relative">
                  <NovelCard novel={novel} />
                  <div className="absolute top-2 right-2 z-10">{getUpdateBadge(novel.updatedAt)}</div>
                </div>
              ))}
            </div>
          )}

          {/* Pagination */}
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
            showPageNumbers={true}
            className="mt-8"
          />
        </div>
      </main>
    </div>
  )
}
