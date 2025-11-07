"use client"

import { useEffect, useMemo, useState } from "react"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { api, type Video } from "@/services/api"
import { formatRelativeTime } from "@/lib/utils"
import { Loader2, Play, Search, Video as VideoIcon } from "lucide-react"

export default function VideosPage() {
  const [videos, setVideos] = useState<Video[]>([])
  const [loading, setLoading] = useState(true)
  const [searchLoading, setSearchLoading] = useState(false)
  const [page, setPage] = useState(0)
  const [pageSize] = useState(12)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [searchInput, setSearchInput] = useState("")
  const [searchTerm, setSearchTerm] = useState("")

  useEffect(() => {
    void fetchVideos()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, searchTerm])

  const fetchVideos = async () => {
    setLoading(true)
    try {
      const response = await api.getVideos({
        page,
        size: pageSize,
        sortBy: "createdAt",
        sortDir: "desc",
        search: searchTerm || undefined,
      })

      if (response.success && response.data) {
        const data = response.data
        setVideos(data.content)
        setTotalPages(data.totalPages)
        setTotalElements(data.totalElements)
      } else {
        console.error("Failed to fetch videos:", response.message)
      }
    } catch (error) {
      console.error("Failed to fetch videos:", error)
    } finally {
      setLoading(false)
      setSearchLoading(false)
    }
  }

  const handleSearch = () => {
    setSearchLoading(true)
    setPage(0)
    setSearchTerm(searchInput.trim())
  }

  const handleClear = () => {
    setSearchInput("")
    setSearchTerm("")
    setPage(0)
  }

  const canGoPrev = useMemo(() => page > 0, [page])
  const canGoNext = useMemo(() => page + 1 < totalPages, [page, totalPages])

  return (
    <div className="min-h-screen bg-background">
      <main className="container mx-auto px-4 py-10">
        <div className="space-y-10">
          <div className="space-y-3 text-center">
            <div className="inline-flex items-center gap-2 rounded-full border px-4 py-1 text-sm text-muted-foreground">
              <Play className="h-4 w-4" />
              Community Video Library
            </div>
            <h1 className="text-4xl font-bold tracking-tight">Watch and Explore</h1>
            <p className="mx-auto max-w-2xl text-muted-foreground">
              Discover videos shared by the community. Browse the latest additions or search by title and
              description to jump straight to what interests you.
            </p>
          </div>

          <Card>
            <CardHeader className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
              <CardTitle className="flex items-center gap-2">
                <VideoIcon className="h-5 w-5" />
                Video Library
              </CardTitle>
              <div className="flex w-full flex-col gap-2 md:w-auto md:flex-row">
                <div className="flex w-full gap-2">
                  <Input
                    placeholder="Search videos..."
                    value={searchInput}
                    onChange={(event) => setSearchInput(event.target.value)}
                    onKeyDown={(event) => {
                      if (event.key === "Enter") {
                        handleSearch()
                      }
                    }}
                  />
                  <Button variant="secondary" onClick={handleSearch} disabled={loading}>
                    {searchLoading ? (
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    ) : (
                      <Search className="mr-2 h-4 w-4" />
                    )}
                    Search
                  </Button>
                </div>
                {(searchTerm || searchInput) && (
                  <Button variant="ghost" onClick={handleClear}>
                    Clear
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent className="space-y-8">
              <p className="text-sm text-muted-foreground">
                Showing {(videos?.length ?? 0).toLocaleString()} of {totalElements.toLocaleString()} result
                {totalElements === 1 ? "" : "s"}
              </p>
              {loading ? (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                  {Array.from({ length: 6 }).map((_, index) => (
                    <div key={index} className="space-y-3 rounded-lg border p-4 animate-pulse">
                      <div className="aspect-video w-full rounded bg-muted" />
                      <div className="h-4 w-3/4 rounded bg-muted" />
                      <div className="h-3 w-1/2 rounded bg-muted" />
                    </div>
                  ))}
                </div>
              ) : videos.length === 0 ? (
                <div className="flex flex-col items-center justify-center gap-3 py-20 text-center">
                  <VideoIcon className="h-12 w-12 text-muted-foreground" />
                  <div className="space-y-1">
                    <p className="text-lg font-semibold">No videos found</p>
                    <p className="text-sm text-muted-foreground">
                      {searchTerm
                        ? "Try a different search or clear the current filters."
                        : "Check back later for new additions from the community."}
                    </p>
                  </div>
                </div>
              ) : (
                <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
                  {videos.map((video) => (
                    <Card key={video.id} className="overflow-hidden">
                      <div className="aspect-video bg-muted">
                        <iframe
                          src={video.embedUrl}
                          title={video.title}
                          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                          allowFullScreen
                          loading="lazy"
                          className="h-full w-full"
                        />
                      </div>
                      <CardContent className="space-y-3 p-5">
                        <div className="flex items-start justify-between gap-3">
                          <h3 className="text-lg font-semibold leading-tight">{video.title}</h3>
                          <Badge variant="secondary">{video.platform.toLowerCase()}</Badge>
                        </div>
                        {video.description && (
                          <p className="text-sm text-muted-foreground line-clamp-3">{video.description}</p>
                        )}
                        <div className="flex items-center justify-between text-xs text-muted-foreground">
                          <span>Added {formatRelativeTime(video.createdAt)}</span>
                          <a
                            href={video.videoUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-primary hover:underline"
                          >
                            Open source
                          </a>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              )}

              {totalPages > 1 && (
                <div className="flex items-center justify-between border-t pt-4">
                  <Button
                    variant="outline"
                    onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
                    disabled={!canGoPrev || loading}
                  >
                    Previous
                  </Button>
                  <p className="text-sm text-muted-foreground">
                    Page {page + 1} of {Math.max(totalPages, 1)}
                  </p>
                  <Button
                    variant="outline"
                    onClick={() => setPage((prev) => (canGoNext ? prev + 1 : prev))}
                    disabled={!canGoNext || loading}
                  >
                    Next
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </main>
    </div>
  )
}
