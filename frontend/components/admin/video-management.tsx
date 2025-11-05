"use client"

import { useEffect, useMemo, useState } from "react"
import { PlayCircle, Search, Video as VideoIcon, Loader2, RotateCw } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Badge } from "@/components/ui/badge"
import { useToast } from "@/hooks/use-toast"
import { api, type CreateVideoPayload, type Video } from "@/lib/api"
import { formatRelativeTime } from "@/lib/utils"

interface VideoFormState extends CreateVideoPayload {}

const INITIAL_FORM: VideoFormState = {
  title: "",
  description: "",
  videoUrl: "",
}

export function VideoManagement() {
  const { toast } = useToast()
  const [videos, setVideos] = useState<Video[]>([])
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)
  const [page, setPage] = useState(0)
  const [pageSize] = useState(10)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [searchInput, setSearchInput] = useState("")
  const [searchTerm, setSearchTerm] = useState("")
  const [form, setForm] = useState<VideoFormState>(INITIAL_FORM)

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
        toast({
          variant: "destructive",
          title: "Failed to load videos",
          description: response.message || "Please try again later.",
        })
      }
    } catch (error: any) {
      toast({
        variant: "destructive",
        title: "Failed to load videos",
        description: error?.message ?? "Please try again later.",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleCreate = async () => {
    if (!form.title.trim() || !form.videoUrl.trim()) {
      toast({
        variant: "destructive",
        title: "Missing information",
        description: "Title and video URL are required.",
      })
      return
    }

    setCreating(true)
    try {
      const payload: CreateVideoPayload = {
        title: form.title.trim(),
        description: form.description?.trim(),
        videoUrl: form.videoUrl.trim(),
      }
      const response = await api.createVideo(payload)
      if (response.success) {
        toast({
          title: "Video added",
          description: `${response.data.title} is now available to users.`,
        })
        setForm(INITIAL_FORM)
        setPage(0)
        setSearchTerm("")
        setSearchInput("")
        await fetchVideos()
      } else {
        toast({
          variant: "destructive",
          title: "Unable to add video",
          description: response.message || "Please verify the information and try again.",
        })
      }
    } catch (error: any) {
      toast({
        variant: "destructive",
        title: "Unable to add video",
        description: error?.message ?? "Please verify the information and try again.",
      })
    } finally {
      setCreating(false)
    }
  }

  const handleSearch = () => {
    setPage(0)
    setSearchTerm(searchInput.trim())
  }

  const handleClearSearch = () => {
    setSearchInput("")
    setSearchTerm("")
    setPage(0)
  }

  const canGoPrev = useMemo(() => page > 0, [page])
  const canGoNext = useMemo(() => page + 1 < totalPages, [page, totalPages])

  return (
    <div className="space-y-8">
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              <VideoIcon className="h-5 w-5" />
              Add New Video
            </CardTitle>
            <p className="text-sm text-muted-foreground">Support YouTube and Facebook video URLs.</p>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-2">
              <label className="text-sm font-medium">Title</label>
              <Input
                value={form.title}
                onChange={(event) => setForm((prev) => ({ ...prev, title: event.target.value }))}
                placeholder="Enter a descriptive title"
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Video URL</label>
              <Input
                value={form.videoUrl}
                onChange={(event) => setForm((prev) => ({ ...prev, videoUrl: event.target.value }))}
                placeholder="https://www.youtube.com/watch?v=..."
              />
            </div>
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">Description</label>
            <Textarea
              value={form.description}
              onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))}
              placeholder="Optional context or notes about the video"
              rows={4}
            />
          </div>
          <div className="flex items-center justify-end gap-2">
            <Button
              onClick={handleCreate}
              disabled={creating}
            >
              {creating ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Saving...
                </>
              ) : (
                <>
                  <PlayCircle className="mr-2 h-4 w-4" />
                  Add Video
                </>
              )}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              <PlayCircle className="h-5 w-5" />
              Video Library
            </CardTitle>
            <p className="text-sm text-muted-foreground">
              {totalElements.toLocaleString()} video{totalElements === 1 ? "" : "s"} available
            </p>
          </div>
          <div className="flex w-full flex-col gap-2 md:w-auto md:flex-row">
            <div className="flex w-full gap-2">
              <Input
                value={searchInput}
                onChange={(event) => setSearchInput(event.target.value)}
                placeholder="Search by title or description"
                onKeyDown={(event) => {
                  if (event.key === "Enter") {
                    handleSearch()
                  }
                }}
              />
              <Button variant="secondary" onClick={handleSearch} disabled={loading}>
                <Search className="mr-2 h-4 w-4" />
                Search
              </Button>
            </div>
            {searchTerm && (
              <Button variant="ghost" onClick={handleClearSearch} className="md:w-auto">
                <RotateCw className="mr-2 h-4 w-4" />
                Reset
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-6">
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
            <div className="flex flex-col items-center justify-center gap-2 py-16 text-center">
              <VideoIcon className="h-10 w-10 text-muted-foreground" />
              <p className="text-sm text-muted-foreground">
                {searchTerm ? "No videos match your search." : "No videos have been added yet."}
              </p>
            </div>
          ) : (
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {videos.map((video) => (
                <div key={video.id} className="space-y-3 rounded-lg border p-4">
                  <div className="aspect-video overflow-hidden rounded-md border">
                    <iframe
                      src={video.embedUrl}
                      title={video.title}
                      allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                      allowFullScreen
                      loading="lazy"
                      className="h-full w-full"
                    />
                  </div>
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="text-base font-semibold leading-tight">{video.title}</h3>
                    <Badge variant="secondary">{video.platform.toLowerCase()}</Badge>
                  </div>
                  {video.description && (
                    <p className="text-sm text-muted-foreground line-clamp-3">{video.description}</p>
                  )}
                  <p className="text-xs text-muted-foreground">
                    Added {formatRelativeTime(video.createdAt)}
                  </p>
                  <a
                    href={video.videoUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="text-sm text-primary hover:underline"
                  >
                    View on source platform
                  </a>
                </div>
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
  )
}
