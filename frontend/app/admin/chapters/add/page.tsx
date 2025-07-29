"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { ArrowLeft, BookOpen, FileText, Clock, Target } from "lucide-react"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"
import { api, type Novel } from "@/lib/api"

interface ChapterFormData {
  novelId: string
  title: string
  chapterNumber: number
  content: string
}

export default function AddChapterPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const { toast } = useToast()
  const [loading, setLoading] = useState(false)
  const [novels, setNovels] = useState<Novel[]>([])
  const [loadingNovels, setLoadingNovels] = useState(true)

  const [formData, setFormData] = useState<ChapterFormData>({
    novelId: searchParams.get("novelId") || "",
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
    fetchNovels()
  }, [])

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
      toast({
        title: "Error",
        description: "Failed to load novels",
        variant: "destructive",
      })
    } finally {
      setLoadingNovels(false)
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
      const response = await api.request("/api/chapters", {
        method: "POST",
        body: JSON.stringify(formData),
      })

      if (response.success) {
        toast({
          title: "Success",
          description: "Chapter created successfully",
        })
        router.push("/admin/chapters")
      }
    } catch (error) {
      console.error("Error creating chapter:", error)
      toast({
        title: "Error",
        description: "Failed to create chapter",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthGuard requireAdmin>
      <div className="min-h-screen bg-background">
        <Header />

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
                <h1 className="text-3xl font-bold">Add New Chapter</h1>
                <p className="text-muted-foreground">Create a new chapter for a novel</p>
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
                      <CardDescription>Enter the basic details of the chapter</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div className="space-y-2">
                        <Label htmlFor="novelId">Novel *</Label>
                        <Select
                          value={formData.novelId}
                          onValueChange={(value) => handleInputChange("novelId", value)}
                          disabled={loadingNovels}
                        >
                          <SelectTrigger>
                            <SelectValue placeholder={loadingNovels ? "Loading novels..." : "Select a novel"} />
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
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle>Chapter Content</CardTitle>
                      <CardDescription>Write the chapter content</CardDescription>
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

                  {/* Writing Tips */}
                  <Card>
                    <CardHeader>
                      <CardTitle>Writing Tips</CardTitle>
                      <CardDescription>Guidelines for great chapters</CardDescription>
                    </CardHeader>
                    <CardContent className="space-y-3">
                      <div className="flex items-start gap-2">
                        <BookOpen className="h-4 w-4 text-blue-500 mt-0.5" />
                        <div>
                          <p className="text-sm font-medium">Chapter Length</p>
                          <p className="text-xs text-muted-foreground">Aim for 1,500-3,000 words per chapter</p>
                        </div>
                      </div>
                      <div className="flex items-start gap-2">
                        <Target className="h-4 w-4 text-green-500 mt-0.5" />
                        <div>
                          <p className="text-sm font-medium">Clear Structure</p>
                          <p className="text-xs text-muted-foreground">
                            Start with action, build tension, end with a hook
                          </p>
                        </div>
                      </div>
                      <div className="flex items-start gap-2">
                        <FileText className="h-4 w-4 text-purple-500 mt-0.5" />
                        <div>
                          <p className="text-sm font-medium">Formatting</p>
                          <p className="text-xs text-muted-foreground">
                            Use line breaks for dialogue and scene changes
                          </p>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Actions */}
                  <Card>
                    <CardContent className="pt-6">
                      <div className="space-y-2">
                        <Button type="submit" className="w-full" disabled={loading || !formData.novelId}>
                          {loading ? "Creating..." : "Create Chapter"}
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
