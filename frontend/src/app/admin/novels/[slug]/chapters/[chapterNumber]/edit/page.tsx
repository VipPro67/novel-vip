"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useRef } from "react"
import { useParams, useRouter } from "next/navigation"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ArrowLeft, BookOpen, FileText, Clock, Target, Loader2 } from "lucide-react"
import RichTextEditor from "@/components/rich-text-editor"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { api, type Novel } from "@/services/api"

interface ChapterFormData {
  novelId: string
  title: string
  chapterNumber: number
}

interface ChapterContentState {
  contentHtml: string
  contentText: string
  format: 'HTML' | 'TEXT'
}

// using shared RichTextEditor

interface ChapterDetail {
  id: string
  chapterNumber: number
  title: string
  novelId: string
  novelTitle: string
  jsonUrl: string
  audioUrl?: string
  createdAt: string
  updatedAt: string
}

export default function EditChapterPage() {
  const router = useRouter()
  const params = useParams()
  const slug = params.slug as string
  const chapterNumber = Number.parseInt(params.chapterNumber as string)

  const { toast } = useToast()
  const [loading, setLoading] = useState(false)
  const [initialLoading, setInitialLoading] = useState(true)
  const [novels, setNovels] = useState<Novel[]>([])
  const [chapter, setChapter] = useState<ChapterDetail | null>(null)

  const [formData, setFormData] = useState<ChapterFormData>({
    novelId: "",
    title: "",
    chapterNumber: 1,
  })

  const [contentState, setContentState] = useState<ChapterContentState>({ contentHtml: "", contentText: "", format: 'HTML' })
  

  const [stats, setStats] = useState({ wordCount: 0, charCount: 0, readingTime: 0 })

  useEffect(() => {
    fetchChapter()
    fetchNovels()
  }, [params.chapterId])

  useEffect(() => {
    const source = contentState.format === 'HTML' ? (contentState.contentHtml || '') : (contentState.contentText || '')
    const words = source.trim().split(/\s+/).filter((word) => word.length > 0).length
    const chars = source.length
    const readingTime = Math.ceil(words / 200)
    setStats({ wordCount: words, charCount: chars, readingTime })
  }, [contentState])

  const fetchChapter = async () => {
    try {
      const response = await api.getChapterByNumber2(slug, chapterNumber)
      if (response.success) {
        const chapterData = response.data as ChapterDetail
        setChapter(chapterData)

        // fetch content from jsonUrl if available
        let content = ""
        if (chapterData.jsonUrl) {
          try {
            const contentResponse = await fetch(chapterData.jsonUrl)
            const contentData = await contentResponse.json()
            content = contentData.content || ""
          } catch (error) {
            console.error("Error fetching chapter content:", error)
          }
        }
        // populate both content states; allow user to pick format
        setFormData({ novelId: chapterData.novelId, title: chapterData.title, chapterNumber: chapterData.chapterNumber })
        setContentState((prev) => ({ ...prev, contentHtml: content, contentText: content }))
      }
    } catch (error) {
      console.error("Error fetching chapter:", error)
      toast({ title: "Error", description: "Failed to load chapter", variant: "destructive" })
    } finally {
      setInitialLoading(false)
    }
  }

  const fetchNovels = async () => {
    try {
      const response = await api.getNovels({ page: 0, size: 100, sortBy: "title", sortDir: "asc" })
      if (response.success) setNovels(response.data.content)
    } catch (error) {
      console.error("Error fetching novels:", error)
    }
  }

  const handleInputChange = (field: keyof ChapterFormData, value: string | number) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      // build payload based on chosen format
  const payload: any = { title: formData.title, chapterNumber: formData.chapterNumber, novelId: formData.novelId, format: contentState.format }
  // backend updateChapter expects 'content' field; put HTML content into content when format is HTML
  payload.content = contentState.format === 'HTML' ? contentState.contentHtml : contentState.contentText
      const response = await api.updateChapter(chapter?.id, payload)
      if (response.success) {
        toast({ title: "Success", description: "Chapter updated successfully" })
        router.push(`/admin/novels/${formData.novelId}/chapters`)
      }
    } catch (error) {
      console.error("Error updating chapter:", error)
      toast({ title: "Error", description: "Failed to update chapter", variant: "destructive" })
    } finally {
      setLoading(false)
    }
  }

  if (initialLoading) {
    return (
      <AuthGuard requireRole="ADMIN">
        <div className="min-h-screen bg-background">
          <main className="container mx-auto px-4 py-8">
            <div className="flex items-center justify-center h-64">
              <Loader2 className="h-8 w-8 animate-spin" />
            </div>
          </main>
        </div>
      </AuthGuard>
    )
  }

  if (!chapter) {
    return (
      <AuthGuard requireRole="ADMIN">
        <div className="min-h-screen bg-background">
          <main className="container mx-auto px-4 py-8">
            <div className="text-center">
              <h1 className="text-2xl font-bold">Chapter not found</h1>
              <Link href="/admin/novels">
                <Button className="mt-4">Back to Novels</Button>
              </Link>
            </div>
          </main>
        </div>
      </AuthGuard>
    )
  }

  return (
    <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
          <main className="container mx-auto px-4 py-8">
          <div className="space-y-6">
            <div className="flex items-center gap-4">
              <Link href={`/admin/novels/${chapter.novelId}/chapters`}>
                <Button variant="outline" size="sm">
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back to Chapters
                </Button>
              </Link>
              <div>
                <h1 className="text-3xl font-bold">Edit Chapter</h1>
                <p className="text-muted-foreground">Update chapter information and content</p>
              </div>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2 space-y-6">
                                    <Card>
                    <CardHeader>
                      <CardTitle>Chapter Info</CardTitle>
                      <CardDescription>Additional chapter details</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      <div className="flex items-start gap-2">
                        <BookOpen className="h-4 w-4 text-blue-500 mt-0.5" />
                        <div>
                          <p className="text-sm font-medium">Novel</p>
                          <p className="text-xs text-muted-foreground">{chapter.novelTitle}</p>
                        </div>
                      </div>
                      <div className="flex items-start gap-2">
                        <FileText className="h-4 w-4 text-purple-500 mt-0.5" />
                        <div>
                          <p className="text-sm font-medium">Chapter Title</p>
                          <p className="text-xs text-muted-foreground">{chapter.title}</p>
                        </div>
                      </div>
                      <div className="flex items-start gap-2">
                        <Target className="h-4 w-4 text-green-500 mt-0.5" />
                        <div>
                          <p className="text-sm font-medium">Chapter #{chapter.chapterNumber}</p>
                          <p className="text-xs text-muted-foreground">Position in series</p>
                        </div>
                      </div>
                      {chapter.audioUrl && (
                        <div className="flex items-start gap-2">
                          <FileText className="h-4 w-4 text-purple-500 mt-0.5" />
                          <div>
                            <p className="text-sm font-medium">Audio Available</p>
                            <p className="text-xs text-muted-foreground">Chapter has audio version</p>
                          </div>
                        </div>
                      )}
                    </CardContent>
                  </Card>


                  <Card>
                    <CardHeader>
                      <CardTitle>Chapter Content</CardTitle>
                      <CardDescription>Edit the chapter content</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-2">
                        <div className="flex items-center justify-between">
                          <Label htmlFor="content">Content *</Label>
                          <div className="flex items-center gap-2">
                            <Label className="text-sm">Type</Label>
                            <Select value={contentState.format} onValueChange={(v) => setContentState((s) => ({ ...s, format: v as 'HTML' | 'TEXT' }))}>
                              <SelectTrigger>
                                <SelectValue placeholder="Select content type" />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="HTML">HTML (Rich)</SelectItem>
                                <SelectItem value="TEXT">Plain Text</SelectItem>
                              </SelectContent>
                            </Select>
                          </div>
                        </div>
                        {contentState.format === 'HTML' ? (
                          <RichTextEditor content={contentState.contentHtml} onChange={(c) => setContentState((s) => ({ ...s, contentHtml: c }))} />
                        ) : (
                          <Textarea id="content" value={contentState.contentText} onChange={(e) => setContentState((s) => ({ ...s, contentText: e.target.value }))} placeholder="Start writing your chapter here..." rows={20} className="min-h-[500px] font-mono" required />
                        )}
                      </div>
                    </CardContent>
                  </Card>
                </div>

                <div className="space-y-6">
                  <Card>
                    <CardHeader>
                      <CardTitle>Writing Statistics</CardTitle>
                      <CardDescription>Track your writing progress</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="flex items-center gap-3">
                        <FileText className="h-5 w-5 text-blue-500" />
                        <div>
                          <p className="text-sm font-medium">Word Count</p>
                          <p className="text-2xl font-bold">{stats.wordCount.toLocaleString()}</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <Target className="h-5 w-5 text-green-500" />
                        <div>
                          <p className="text-sm font-medium">Characters</p>
                          <p className="text-2xl font-bold">{stats.charCount.toLocaleString()}</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3">
                        <Clock className="h-5 w-5 text-purple-500" />
                        <div>
                          <p className="text-sm font-medium">Reading Time</p>
                          <p className="text-2xl font-bold">{stats.readingTime} min</p>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardContent className="pt-6">
                      <div className="space-y-2">
                        <Button type="submit" className="w-full" disabled={loading}>{loading ? "Updating..." : "Update Chapter"}</Button>
                        <Button type="button" variant="outline" className="w-full bg-transparent" asChild>
                          <Link href={`/admin/novels/${chapter.novelId}/chapters`}>Cancel</Link>
                        </Button>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </div>
            </form>
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
