"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useRouter } from "@/navigation"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ArrowLeft, BookOpen, FileText, Clock, Target, Loader2 } from "lucide-react"
import { Link } from "@/navigation"
import { useToast } from "@/hooks/use-toast"
import { api, type Novel } from "@/services/api"

interface ChapterFormData {
  novelId: string
  title: string
  chapterNumber: number
  content: string
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

export default function EditChapterPage({ params }: { params: { id: string } }) {
  const router = useRouter()
  const { toast } = useToast()
  const [loading, setLoading] = useState(false)
  const [initialLoading, setInitialLoading] = useState(true)
  const [novels, setNovels] = useState<Novel[]>([])
  const [chapter, setChapter] = useState<ChapterDetail | null>(null)

  const [formData, setFormData] = useState<ChapterFormData>({
    novelId: "",
    title: "",
    chapterNumber: 1,
    content: "",
  })

  const [stats, setStats] = useState({
    wordCount: 0,
    charCount: 0,
    readingTime: 0,
  })

  useEffect(() => {
    fetchChapter()
    fetchNovels()
  }, [params.id])

  useEffect(() => {
    // Calculate stats when content changes
    const words = formData.content
      .trim()
      .split(/\s+/)
      .filter((word) => word.length > 0).length
    const chars = formData.content.length
    const readingTime = Math.ceil(words / 200) // Assuming 200 words per minute

    setStats({
      wordCount: words,
      charCount: chars,
      readingTime: readingTime,
    })
  }, [formData.content])

  const fetchChapter = async () => {
    try {
      const response = await api.request(`/api/chapters/${params.id}`)
      if (response.success) {
        const chapterData = response.data
        setChapter(chapterData)

        // Fetch chapter content from JSON URL
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

        setFormData({
          novelId: chapterData.novelId,
          title: chapterData.title,
          chapterNumber: chapterData.chapterNumber,
          content: content,
        })
      }
    } catch (error) {
      console.error("Error fetching chapter:", error)
      toast({
        title: "Error",
        description: "Failed to load chapter",
        variant: "destructive",
      })
    } finally {
      setInitialLoading(false)
    }
  }

  const fetchNovels = async () => {
    try {
      const response = await api.getNovels({
        page: 0,
        size: 100,
        sortBy: "title",
        sortDir: "asc",
      })

      if (response.success) {
        setNovels(response.data.content)
      }
    } catch (error) {
      console.error("Error fetching novels:", error)
    }
  }

  const handleInputChange = (field: keyof ChapterFormData, value: string | number) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      const response = await api.request(`/api/chapters/${params.id}`, {
        method: "PUT",
        body: JSON.stringify(formData),
      })

      if (response.success) {
        toast({
          title: "Success",
          description: "Chapter updated successfully",
        })
        router.push("/admin/chapters")
      }
    } catch (error) {
      console.error("Error updating chapter:", error)
      toast({
        title: "Error",
        description: "Failed to update chapter",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  if (initialLoading) {
    return (
      <AuthGuard requireRole={["ADMIN","AUTHOR"]}>
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
      <AuthGuard requireRole={["ADMIN","AUTHOR"]}>
        <div className="min-h-screen bg-background">
          <main className="container mx-auto px-4 py-8">
            <div className="text-center">
              <h1 className="text-2xl font-bold">Chapter not found</h1>
              <Link href="/admin/chapters">
                <Button className="mt-4">Back to Chapters</Button>
              </Link>
            </div>
          </main>
        </div>
      </AuthGuard>
    )
  }

  return (
    <AuthGuard requireRole={["ADMIN","AUTHOR"]}>
      <div className="min-h-screen bg-background">
          <main className="container mx-auto px-4 py-8">
          <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
              <Link href="/admin/chapters">
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

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Main Form */}
                <div className="lg:col-span-2 space-y-6">
                  <Card>
                    <CardHeader>
                      <CardTitle>Chapter Information</CardTitle>
                      <CardDescription>Update the basic details of the chapter</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="space-y-2">
                        <Label htmlFor="novelId">Novel *</Label>
                        <Select value={formData.novelId} onValueChange={(value) => handleInputChange("novelId", value)}>
                          <SelectTrigger>
                            <SelectValue placeholder="Select a novel" />
                          </SelectTrigger>
                          <SelectContent>
                            {novels.map((novel) => (
                              <SelectItem key={novel.id} value={novel.id}>
                                {novel.title} - {novel.author}
                              </SelectItem>
                            ))}
                          </SelectContent>
                        </Select>
                      </div>

                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="space-y-2">
                          <Label htmlFor="title">Chapter Title *</Label>
                          <Input
                            id="title"
                            value={formData.title}
                            onChange={(e) => handleInputChange("title", e.target.value)}
                            placeholder="Enter chapter title"
                            required
                          />
                        </div>
                        <div className="space-y-2">
                          <Label htmlFor="chapterNumber">Chapter Number *</Label>
                          <Input
                            id="chapterNumber"
                            type="number"
                            min="1"
                            value={formData.chapterNumber}
                            onChange={(e) => handleInputChange("chapterNumber", Number.parseInt(e.target.value) || 1)}
                            placeholder="1"
                            required
                          />
                        </div>
                      </div>

                      {/* Chapter Metadata */}
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t">
                        <div>
                          <Label className="text-sm text-muted-foreground">Created</Label>
                          <p className="text-sm">{new Date(chapter.createdAt).toLocaleDateString()}</p>
                        </div>
                        <div>
                          <Label className="text-sm text-muted-foreground">Last Updated</Label>
                          <p className="text-sm">{new Date(chapter.updatedAt).toLocaleDateString()}</p>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle>Chapter Content</CardTitle>
                      <CardDescription>Edit the chapter content</CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-2">
                        <Label htmlFor="content">Content *</Label>
                        <Textarea
                          id="content"
                          value={formData.content}
                          onChange={(e) => handleInputChange("content", e.target.value)}
                          placeholder="Start writing your chapter here..."
                          rows={20}
                          className="min-h-[500px] font-mono"
                          required
                        />
                      </div>
                    </CardContent>
                  </Card>
                </div>

                {/* Sidebar */}
                <div className="space-y-6">
                  {/* Writing Statistics */}
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

                  {/* Chapter Info */}
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

                  {/* Actions */}
                  <Card>
                    <CardContent className="pt-6">
                      <div className="space-y-2">
                        <Button type="submit" className="w-full" disabled={loading}>
                          {loading ? "Updating..." : "Update Chapter"}
                        </Button>
                        <Button type="button" variant="outline" className="w-full bg-transparent" asChild>
                          <Link href="/admin/chapters">Cancel</Link>
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
