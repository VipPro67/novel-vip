"use client"

import { useState, useEffect } from "react"
import { useRouter } from "@/navigation"
import { ChevronDown, ChevronUp, List, Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { api, type Chapter } from "@/services/api"
import { useToast } from "@/hooks/use-toast"

interface ChapterNavigationProps {
  novelSlug: string
  currentChapterNumber: number
  currentChapterTitle: string
}

export default function ChapterNavigation({
  novelSlug,
  currentChapterNumber,
  currentChapterTitle,
}: ChapterNavigationProps) {
  const router = useRouter()
  const { toast } = useToast()
  const [isOpen, setIsOpen] = useState(false)
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [loading, setLoading] = useState(false)
  const [loaded, setLoaded] = useState(false)

  useEffect(() => {
    if (isOpen && !loaded) {
      loadChapters()
    }
  }, [isOpen, loaded])

  const loadChapters = async () => {
    setLoading(true)
    try {
      const response = await api.getChapters(novelSlug, {
        page: 0,
        size: 1000, // Load all chapters
        sortBy: "chapterNumber",
        sortDir: "asc",
      })

      if (response.success) {
        setChapters(response.data.content || [])
        setLoaded(true)
      } else {
        toast({
          title: "Error",
          description: "Failed to load chapters",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error("Failed to load chapters:", error)
      toast({
        title: "Error",
        description: "Failed to load chapters",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleChapterClick = (chapterNumber: number) => {
    if (chapterNumber !== currentChapterNumber) {
      router.push(`/novels/${novelSlug}/chapters/${chapterNumber}`)
      setIsOpen(false)
    }
  }

  return (
    <div className="space-y-3">
      {/* Current Chapter Display & Toggle */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <List className="h-5 w-5 text-muted-foreground" />
          <div>
            <p className="text-sm font-medium">Chapter {currentChapterNumber}</p>
            <p className="text-xs text-muted-foreground truncate max-w-[300px]">{currentChapterTitle}</p>
          </div>
        </div>
        <Button variant="ghost" size="sm" onClick={() => setIsOpen(!isOpen)} className="ml-2">
          {isOpen ? (
            <>
              <ChevronUp className="h-4 w-4 mr-1" />
              Hide
            </>
          ) : (
            <>
              <ChevronDown className="h-4 w-4 mr-1" />
              All Chapters
            </>
          )}
        </Button>
      </div>

      {/* Chapter List */}
      {isOpen && (
        <div className="border-t pt-3">
          {loading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
            </div>
          ) : chapters.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-sm text-muted-foreground">No chapters available</p>
            </div>
          ) : (
            <div className="max-h-[400px] overflow-y-auto pr-4">
              <div className="space-y-2">
                {chapters.map((chapter) => {
                  const isCurrent = chapter.chapterNumber === currentChapterNumber
                  return (
                    <button
                      key={chapter.id}
                      onClick={() => handleChapterClick(chapter.chapterNumber)}
                      disabled={isCurrent}
                      className={`
                        w-full text-left p-3 rounded-lg border transition-colors
                        ${
                          isCurrent
                            ? "bg-primary/10 border-primary cursor-default"
                            : "hover:bg-muted border-transparent cursor-pointer"
                        }
                      `}
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center space-x-2 mb-1">
                            <span className="text-sm font-medium">Chapter {chapter.chapterNumber}</span>
                            {isCurrent && (
                              <Badge variant="default" className="text-xs">
                                Current
                              </Badge>
                            )}
                          </div>
                          <p className="text-sm text-muted-foreground truncate">{chapter.title}</p>
                        </div>
                      </div>
                    </button>
                  )
                })}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  )
}
