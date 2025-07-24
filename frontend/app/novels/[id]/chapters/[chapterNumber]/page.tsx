"use client"

import { useEffect, useState } from "react"
import { useParams, useRouter } from "next/navigation"
import Link from "next/link"
import { ChevronLeft, ChevronRight, Settings, Bookmark, Home, List } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { useAuth } from "@/components/providers/auth-provider"
import { useToast } from "@/hooks/use-toast"
import { api, type ChapterDetail, type ReaderSettings } from "@/lib/api"

export default function ChapterReaderPage() {
  const params = useParams()
  const router = useRouter()
  const { isAuthenticated } = useAuth()
  const { toast } = useToast()
  const [chapter, setChapter] = useState<ChapterDetail | null>(null)
  const [chapterContent, setChapterContent] = useState("")
  const [readerSettings, setReaderSettings] = useState<ReaderSettings | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (params.id && params.chapterNumber) {
      fetchChapterData()
      if (isAuthenticated) {
        fetchReaderSettings()
      }
    }
  }, [params.id, params.chapterNumber])

  const fetchChapterData = async () => {
    try {
      const response = await api.getChapterByNumber(
        params.id as string,
        Number.parseInt(params.chapterNumber as string),
      )

      if (response.success) {
        setChapter(response.data)

        // Record reading history
        if (isAuthenticated) {
          await api.addReadingHistory(response.data.id)
        }

        // Fetch chapter content from JSON URL
        if (response.data.jsonUrl) {
          try {
            const contentResponse = await fetch(response.data.jsonUrl)
            const contentData = await contentResponse.json()
            setChapterContent(contentData.content || "Chapter content not available.")
          } catch (error) {
            setChapterContent("Failed to load chapter content.")
          }
        } else {
          setChapterContent("Chapter content not available.")
        }
      }
    } catch (error) {
      console.error("Failed to fetch chapter:", error)
    } finally {
      setLoading(false)
    }
  }

  const fetchReaderSettings = async () => {
    try {
      const response = await api.getReaderSettings()
      if (response.success) {
        setReaderSettings(response.data)
      }
    } catch (error) {
      console.error("Failed to fetch reader settings:", error)
    }
  }

  const handleBookmark = async () => {
    if (!isAuthenticated) {
      router.push("/login")
      return
    }

    if (!chapter) return

    try {
      await api.createBookmark({
        chapterId: chapter.id,
        novelId: chapter.novelId,
        note: `Reading ${chapter.title}`,
        progress: 0,
      })
      toast({ title: "Bookmark added" })
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to add bookmark",
        variant: "destructive",
      })
    }
  }

  const navigateChapter = (direction: "prev" | "next") => {
    if (!chapter) return

    const currentChapter = Number.parseInt(params.chapterNumber as string)
    const newChapter = direction === "prev" ? currentChapter - 1 : currentChapter + 1

    if (newChapter > 0) {
      router.push(`/novels/${params.id}/chapters/${newChapter}`)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-background">
        <div className="container py-8">
          <div className="animate-pulse space-y-4">
            <div className="h-8 bg-muted rounded" />
            <div className="h-4 bg-muted rounded" />
            <div className="space-y-2">
              {Array.from({ length: 10 }).map((_, i) => (
                <div key={i} className="h-4 bg-muted rounded" />
              ))}
            </div>
          </div>
        </div>
      </div>
    )
  }

  if (!chapter) {
    return (
      <div className="min-h-screen bg-background">
        <div className="container py-8">
          <div className="text-center">
            <h1 className="text-2xl font-bold">Chapter not found</h1>
            <Button asChild className="mt-4">
              <Link href={`/novels/${params.id}`}>Back to Novel</Link>
            </Button>
          </div>
        </div>
      </div>
    )
  }

  const readerStyle = readerSettings
    ? {
        fontSize: `${readerSettings.fontSize}px`,
        fontFamily: readerSettings.fontFamily,
        lineHeight: readerSettings.lineHeight,
        margin: `0 ${readerSettings.marginSize}px`,
      }
    : {}

  return (
    <div className="min-h-screen bg-background">
      {/* Reader Header */}
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur">
        <div className="container flex h-14 items-center justify-between">
          <div className="flex items-center space-x-4">
            <Button variant="ghost" size="icon" asChild>
              <Link href="/">
                <Home className="h-4 w-4" />
              </Link>
            </Button>
            <Button variant="ghost" size="icon" asChild>
              <Link href={`/novels/${params.id}`}>
                <List className="h-4 w-4" />
              </Link>
            </Button>
            <Separator orientation="vertical" className="h-6" />
            <div>
              <p className="font-medium text-sm">{chapter.novelTitle}</p>
              <p className="text-xs text-muted-foreground">
                Chapter {chapter.chapterNumber}: {chapter.title}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-2">
            <Button variant="ghost" size="icon" onClick={handleBookmark}>
              <Bookmark className="h-4 w-4" />
            </Button>

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="icon">
                  <Settings className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem asChild>
                  <Link href="/settings">Reader Settings</Link>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>
      </header>

      {/* Chapter Content */}
      <main className="container py-8">
        <Card>
          <CardContent className="p-8">
            <div className="max-w-4xl mx-auto">
              <header className="text-center mb-8">
                <h1 className="text-2xl font-bold mb-2">
                  Chapter {chapter.chapterNumber}: {chapter.title}
                </h1>
                <p className="text-muted-foreground">{chapter.novelTitle}</p>
              </header>

              <div className="prose prose-lg max-w-none dark:prose-invert" style={readerStyle}>
                <div className="whitespace-pre-wrap leading-relaxed">{chapterContent}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Navigation */}
        <div className="flex justify-between items-center mt-8">
          <Button
            variant="outline"
            onClick={() => navigateChapter("prev")}
            disabled={Number.parseInt(params.chapterNumber as string) <= 1}
          >
            <ChevronLeft className="mr-2 h-4 w-4" />
            Previous Chapter
          </Button>

          <Button variant="outline" asChild>
            <Link href={`/novels/${params.id}`}>
              <List className="mr-2 h-4 w-4" />
              Chapter List
            </Link>
          </Button>

          <Button variant="outline" onClick={() => navigateChapter("next")}>
            Next Chapter
            <ChevronRight className="ml-2 h-4 w-4" />
          </Button>
        </div>
      </main>
    </div>
  )
}
