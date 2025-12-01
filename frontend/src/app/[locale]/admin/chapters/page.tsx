"use client"

import { useEffect, useMemo, useState } from "react"
import { useRouter, useSearchParams } from "@/navigation"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Skeleton } from "@/components/ui/skeleton"
import { Textarea } from "@/components/ui/textarea"
import { Separator } from "@/components/ui/separator"
import { Badge } from "@/components/ui/badge"
import { ScrollArea } from "@/components/ui/scroll-area"
import { useToast } from "@/hooks/use-toast"
import { api } from "@/services/api"
import type { Chapter, ChapterDetail, Novel } from "@/models"
import { BookOpen, Loader2, Plus, RefreshCcw, Wand2 } from "lucide-react"

interface ChapterFormState {
  title: string
  chapterNumber: string
  content: string
}

export default function ChaptersPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { toast } = useToast()

  const [novels, setNovels] = useState<Novel[]>([])
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [selectedNovelId, setSelectedNovelId] = useState<string>("")
  const [selectedChapterId, setSelectedChapterId] = useState<string>("")
  const [chapterDetail, setChapterDetail] = useState<ChapterDetail | null>(null)
  const [formState, setFormState] = useState<ChapterFormState>({
    title: "",
    chapterNumber: "",
    content: "",
  })

  const [loadingNovels, setLoadingNovels] = useState(true)
  const [loadingChapters, setLoadingChapters] = useState(false)
  const [loadingDetail, setLoadingDetail] = useState(false)
  const [updatingChapter, setUpdatingChapter] = useState(false)
  const [reindexingChapter, setReindexingChapter] = useState(false)

  const preselectedNovelId = searchParams.get("novelId")
  const preselectedChapterId = searchParams.get("chapterId")

  useEffect(() => {
    void fetchNovels()
  }, [])

  useEffect(() => {
    if (selectedNovelId) {
      void fetchChapters(selectedNovelId)
    }
  }, [selectedNovelId])

  useEffect(() => {
    if (preselectedChapterId) {
      setSelectedChapterId(preselectedChapterId)
      void fetchChapterDetail(preselectedChapterId)
    }
  }, [preselectedChapterId])

  const fetchNovels = async () => {
    try {
      setLoadingNovels(true)
      const response = await api.getNovels({ size: 100, sortBy: "title", sortDir: "asc" })
      if (response.success) {
        setNovels(response.data.content)
        const initialNovelId = preselectedNovelId || response.data.content[0]?.id || ""
        if (initialNovelId) {
          setSelectedNovelId(initialNovelId)
        }
      } else {
        toast({
          title: "Failed to load novels",
          description: response.message || "Unable to fetch novels",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error fetching novels", error)
      toast({
        title: "Failed to load novels",
        description: "An unexpected error occurred",
        variant: "destructive",
      })
    } finally {
      setLoadingNovels(false)
    }
  }

  const fetchChapters = async (novelId: string) => {
    try {
      setLoadingChapters(true)
      const response = await api.getChaptersByNovel(novelId, {
        size: 200,
        sortBy: "chapterNumber",
        sortDir: "asc",
      })

      if (response.success) {
        setChapters(response.data.content)
        if (!selectedChapterId || !response.data.content.find((chapter) => chapter.id === selectedChapterId)) {
          const firstChapterId = response.data.content[0]?.id
          if (firstChapterId) {
            void fetchChapterDetail(firstChapterId)
            setSelectedChapterId(firstChapterId)
          } else {
            resetDetailState()
          }
        }
      } else {
        toast({
          title: "Failed to load chapters",
          description: response.message || "Unable to fetch chapters",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error fetching chapters", error)
      toast({
        title: "Failed to load chapters",
        description: "An unexpected error occurred",
        variant: "destructive",
      })
    } finally {
      setLoadingChapters(false)
    }
  }

  const fetchChapterContent = async (detail: ChapterDetail) => {
    if (!detail.jsonUrl) return ""

    try {
      const response = await fetch(detail.jsonUrl)
      const data = await response.json()
      return data.content || ""
    } catch (error) {
      console.error("Error loading chapter content", error)
      toast({
        title: "Content unavailable",
        description: "Unable to load saved chapter content",
        variant: "destructive",
      })
      return ""
    }
  }

  const fetchChapterDetail = async (chapterId: string) => {
    try {
      setLoadingDetail(true)
      const response = await api.getChapterById(chapterId)
      if (response.success) {
        const detail = response.data
        setChapterDetail(detail)
        const content = await fetchChapterContent(detail)
        setFormState({
          title: detail.title,
          chapterNumber: detail.chapterNumber.toString(),
          content,
        })
      } else {
        toast({
          title: "Failed to load chapter",
          description: response.message || "Unable to fetch chapter details",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error fetching chapter detail", error)
      toast({
        title: "Failed to load chapter",
        description: "An unexpected error occurred",
        variant: "destructive",
      })
    } finally {
      setLoadingDetail(false)
    }
  }

  const resetDetailState = () => {
    setChapterDetail(null)
    setFormState({
      title: "",
      chapterNumber: "",
      content: "",
    })
  }

  const handleUpdateChapter = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!selectedChapterId || !chapterDetail) return

    if (!formState.title || !formState.chapterNumber) {
      toast({
        title: "Missing details",
        description: "Title and chapter number are required",
        variant: "destructive",
      })
      return
    }

    try {
      setUpdatingChapter(true)
      const response = await api.updateChapter(selectedChapterId, {
        title: formState.title,
        chapterNumber: Number.parseInt(formState.chapterNumber, 10),
        contentHtml: formState.content,
        format: "HTML",
        novelId: chapterDetail.novelId,
      })

      if (response.success) {
        toast({
          title: "Chapter updated",
          description: "Changes saved successfully",
        })
        void fetchChapters(chapterDetail.novelId)
      } else {
        toast({
          title: "Update failed",
          description: response.message || "Unable to save chapter",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error updating chapter", error)
      toast({
        title: "Update failed",
        description: "An unexpected error occurred",
        variant: "destructive",
      })
    } finally {
      setUpdatingChapter(false)
    }
  }

  const handleReindexChapter = async () => {
    if (!selectedChapterId) return
    try {
      setReindexingChapter(true)
      const response = await api.reindexChapter(selectedChapterId)
      if (response.success) {
        toast({
          title: "Reindex queued",
          description: "Chapter will be reindexed shortly",
        })
      } else {
        toast({
          title: "Reindex failed",
          description: response.message || "Unable to trigger reindex",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Error triggering reindex", error)
      toast({
        title: "Reindex failed",
        description: "An unexpected error occurred",
        variant: "destructive",
      })
    } finally {
      setReindexingChapter(false)
    }
  }

  const selectedNovel = useMemo(
    () => novels.find((novel) => novel.id === selectedNovelId),
    [novels, selectedNovelId],
  )

  const handleNovelChange = (value: string) => {
    setSelectedNovelId(value)
    setSelectedChapterId("")
    resetDetailState()
  }

  const handleChapterChange = (chapterId: string) => {
    setSelectedChapterId(chapterId)
    void fetchChapterDetail(chapterId)
  }

  const renderChapterList = () => {
    if (loadingChapters) {
      return (
        <div className="space-y-3">
          {[...Array(4)].map((_, index) => (
            <Skeleton key={index} className="h-16 w-full" />
          ))}
        </div>
      )
    }

    if (!chapters.length) {
      return <p className="text-sm text-muted-foreground">No chapters found for this novel.</p>
    }

    return (
      <ScrollArea className="h-[520px] pr-2">
        <div className="space-y-2">
          {chapters.map((chapter) => (
            <button
              key={chapter.id}
              type="button"
              onClick={() => handleChapterChange(chapter.id)}
              className={`w-full rounded-lg border px-4 py-3 text-left transition hover:border-primary hover:bg-primary/5 ${selectedChapterId === chapter.id ? "border-primary bg-primary/5" : "border-border"}`}
            >
              <div className="flex items-center justify-between">
                <div className="space-y-1">
                  <p className="text-sm font-semibold">{chapter.title}</p>
                  <p className="text-xs text-muted-foreground">Chapter {chapter.chapterNumber}</p>
                </div>
                <Badge variant={selectedChapterId === chapter.id ? "default" : "secondary"}>{chapter.chapterNumber}</Badge>
              </div>
            </button>
          ))}
        </div>
      </ScrollArea>
    )
  }

  return (
    <div className="container mx-auto space-y-6 pb-10 pt-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold">Chapters</h1>
          <p className="text-muted-foreground">Browse, edit, and reindex chapters</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" onClick={() => router.refresh()}>
            <RefreshCcw className="mr-2 h-4 w-4" />
            Refresh
          </Button>
          <Button onClick={() => router.push(`/admin/chapters/add${selectedNovelId ? `?novelId=${selectedNovelId}` : ""}`)}>
            <Plus className="mr-2 h-4 w-4" />
            New Chapter
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div className="space-y-1">
              <CardTitle>Chapters by Novel</CardTitle>
              <CardDescription>Select a novel to load its chapters</CardDescription>
            </div>
            <div className="w-full max-w-xs">
              <Label className="mb-1 block text-sm font-medium">Novel</Label>
              <Select value={selectedNovelId} onValueChange={handleNovelChange} disabled={loadingNovels}>
                <SelectTrigger>
                  <SelectValue placeholder={loadingNovels ? "Loading novels..." : "Select novel"} />
                </SelectTrigger>
                <SelectContent>
                  {novels.map((novel) => (
                    <SelectItem key={novel.id} value={novel.id}>
                      {novel.title}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <div className="space-y-4 lg:col-span-1">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold">Chapters</h2>
                <Badge variant="outline" className="text-xs">
                  {loadingChapters ? "Loading" : `${chapters.length} loaded`}
                </Badge>
              </div>
              {renderChapterList()}
            </div>

            <Separator orientation="vertical" className="hidden h-full lg:block" />

            <div className="lg:col-span-2">
              <div className="flex items-center justify-between gap-2">
                <div className="space-y-1">
                  <h2 className="text-lg font-semibold">Chapter Details</h2>
                  <p className="text-sm text-muted-foreground">
                    Edit title, number, and content. Saved changes update navigation automatically.
                  </p>
                </div>
                <Button
                  type="button"
                  variant="secondary"
                  disabled={!selectedChapterId || reindexingChapter}
                  onClick={handleReindexChapter}
                >
                  {reindexingChapter ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Wand2 className="mr-2 h-4 w-4" />}
                  Reindex
                </Button>
              </div>

              <div className="mt-4">
                {loadingDetail && (
                  <div className="space-y-4">
                    <Skeleton className="h-10 w-full" />
                    <Skeleton className="h-10 w-full" />
                    <Skeleton className="h-[320px] w-full" />
                  </div>
                )}

                {!loadingDetail && !chapterDetail && (
                  <div className="rounded-lg border border-dashed p-6 text-center text-sm text-muted-foreground">
                    Select a chapter to view its details
                  </div>
                )}

                {!loadingDetail && chapterDetail && (
                  <form onSubmit={handleUpdateChapter} className="space-y-6">
                    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                      <div className="space-y-2">
                        <Label htmlFor="title">Title</Label>
                        <Input
                          id="title"
                          value={formState.title}
                          onChange={(event) => setFormState((prev) => ({ ...prev, title: event.target.value }))}
                          placeholder="Chapter title"
                          required
                        />
                      </div>
                      <div className="space-y-2">
                        <Label htmlFor="chapterNumber">Chapter Number</Label>
                        <Input
                          id="chapterNumber"
                          type="number"
                          min={1}
                          value={formState.chapterNumber}
                          onChange={(event) => setFormState((prev) => ({ ...prev, chapterNumber: event.target.value }))}
                          required
                        />
                      </div>
                    </div>

                    <div className="space-y-2">
                      <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <BookOpen className="h-4 w-4" />
                        <span>
                          Editing <strong>{selectedNovel?.title || "Novel"}</strong> · Last updated {new Date(chapterDetail.updatedAt).toLocaleString()}
                        </span>
                      </div>
                      <Label htmlFor="content">Content</Label>
                      <Textarea
                        id="content"
                        value={formState.content}
                        onChange={(event) => setFormState((prev) => ({ ...prev, content: event.target.value }))}
                        rows={14}
                        placeholder="Chapter content"
                        className="font-mono"
                      />
                    </div>

                    <div className="flex flex-wrap items-center gap-2">
                      <Button type="submit" disabled={updatingChapter}>
                        {updatingChapter ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <RefreshCcw className="mr-2 h-4 w-4" />}
                        {updatingChapter ? "Saving..." : "Save Changes"}
                      </Button>
                      <Button
                        type="button"
                        variant="outline"
                        onClick={() => chapterDetail && void fetchChapterDetail(chapterDetail.id)}
                        disabled={loadingDetail}
                      >
                        Reload Details
                      </Button>
                    </div>
                  </form>
                )}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
