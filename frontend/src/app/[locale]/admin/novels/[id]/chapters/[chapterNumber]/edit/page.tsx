"use client"

import type React from "react"
import { useState, useEffect } from "react"
import { useParams, useRouter } from "next/navigation"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ArrowLeft, BookOpen, FileText, Clock, Target, Loader2 } from "lucide-react"
import RichTextEditor from "@/components/rich-text-editor"
import { Link } from "@/navigation"
import { useToast } from "@/hooks/use-toast"
import { api } from "@/services/api"

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
  const id = params.id as string
  const chapterNumber = Number.parseInt(params.chapterNumber as string)

  const { toast } = useToast()
  const [infoLoading, setInfoLoading] = useState(false)
  const [contentLoading, setContentLoading] = useState(false)
  const [initialLoading, setInitialLoading] = useState(true)
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
  }, [params.id, params.chapterNumber])

  useEffect(() => {
    const source = contentState.format === 'HTML' ? (contentState.contentHtml || '') : (contentState.contentText || '')
    const words = source.trim().split(/\s+/).filter((word) => word.length > 0).length
    const chars = source.length
    const readingTime = Math.ceil(words / 200)
    setStats({ wordCount: words, charCount: chars, readingTime })
  }, [contentState])

  const fetchChapter = async () => {
    try {
      const response = await api.getChapterByNumber(id, chapterNumber)
      if (response.success) {
        const chapterData = response.data as ChapterDetail
        setChapter(chapterData)

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
        setFormData({ novelId: chapterData.novelId, title: chapterData.title, chapterNumber: chapterData.chapterNumber })
        setContentState({ contentHtml: content, contentText: content, format: 'HTML' })
      }
    } catch (error) {
      console.error("Error fetching chapter:", error)
      toast({ title: "Error", description: "Failed to load chapter", variant: "destructive" })
    } finally {
      setInitialLoading(false)
    }
  }

  const handleInfoChange = (field: keyof ChapterFormData, value: string | number) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const handleInfoSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setInfoLoading(true)
    try {
      const response = await api.updateChapterInfo(chapter!.id, formData)
      if (response.success) {
        toast({ title: "Success", description: "Chapter info updated successfully" })
        if(response.data.chapterNumber !== chapter?.chapterNumber) {
            router.push(`/admin/novels/${response.data.novelId}/chapters/${response.data.chapterNumber}/edit`)
        }
      }
    } catch (error) {
      console.error("Error updating chapter info:", error)
      toast({ title: "Error", description: "Failed to update chapter info", variant: "destructive" })
    } finally {
      setInfoLoading(false)
    }
  }

  const handleContentSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setContentLoading(true)
    try {
      const payload: any = {}
      payload.contentHtml = contentState.contentHtml
      const response = await api.updateChapterContent(chapter!.id, payload)
      if (response.success) {
        toast({ title: "Success", description: "Chapter content updated successfully" })
      }
    } catch (error) {
      console.error("Error updating chapter content:", error)
      toast({ title: "Error", description: "Failed to update chapter content", variant: "destructive" })
    } finally {
      setContentLoading(false)
    }
  }

  if (initialLoading) {
    return (
      <AuthGuard requireRole={["ADMIN", "AUTHOR"]}>
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
      <AuthGuard requireRole={["ADMIN", "AUTHOR"]}>
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
    <AuthGuard requireRole={["ADMIN", "AUTHOR"]}>
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

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div className="lg:col-span-2 space-y-6">
                <form onSubmit={handleInfoSubmit}>
                  <Card>
                    <CardHeader>
                      <CardTitle>Chapter Info</CardTitle>
                      <CardDescription>Update basic chapter details.</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div>
                        <Label>Novel</Label>
                        <Input value={chapter.novelTitle} disabled />
                      </div>
                      <div>
                        <Label htmlFor="title">Title</Label>
                        <Input id="title" value={formData.title} onChange={(e) => handleInfoChange("title", e.target.value)} required />
                      </div>
                      <div>
                        <Label htmlFor="chapterNumber">Chapter Number</Label>
                        <Input id="chapterNumber" type="number" value={formData.chapterNumber} onChange={(e) => handleInfoChange("chapterNumber", Number(e.target.value))} required />
                      </div>
                    </CardContent>
                    <CardContent>
                        <Button type="submit" className="w-full" disabled={infoLoading}>{infoLoading ? "Updating..." : "Update Info"}</Button>
                    </CardContent>
                  </Card>
                </form>

                <form onSubmit={handleContentSubmit}>
                  <Card>
                    <CardHeader>
                      <CardTitle>Chapter Content</CardTitle>
                      <CardDescription>Edit the chapter's text using the rich text editor or plain text.</CardDescription>
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
                    <CardContent>
                        <Button type="submit" className="w-full" disabled={contentLoading}>{contentLoading ? "Updating..." : "Update Content"}</Button>
                    </CardContent>
                  </Card>
                </form>
              </div>

              <div className="space-y-6">
                <Card>
                  <CardHeader>
                    <CardTitle>Writing Statistics</CardTitle>
                    <CardDescription>Track your writing progress.</CardDescription>
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
              </div>
            </div>
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
