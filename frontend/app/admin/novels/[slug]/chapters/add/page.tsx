"use client"

import type React from "react"

import { useState, useEffect, useRef } from "react"
import { useRouter, useSearchParams, useParams } from "next/navigation"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Progress } from "@/components/ui/progress"
import { useToast } from "@/hooks/use-toast"
import { api, type Novel } from "@/lib/api"
import { Type, Upload, FileText, X, BookOpen, Clock, FileCheck, Lightbulb, } from "lucide-react"
import RichTextEditor from "@/components/rich-text-editor"
import Link from "next/link"

// using shared RichTextEditor

export default function AddChapterPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const params = useParams()
  const { toast } = useToast()

  const [novels, setNovels] = useState<Novel[]>([])
  const [selectedNovelId, setSelectedNovelId] = useState("")
  const [title, setTitle] = useState("")
  const [chapterNumber, setChapterNumber] = useState("")
  const [contentHtml, setContent] = useState("")
  const [contentText, setContentText] = useState("")
  const [format, setFormat] = useState<'HTML' | 'TEXT'>('HTML')
  const [isLoading, setIsLoading] = useState(false)
  const [isLoadingNovels, setIsLoadingNovels] = useState(true)

  // prefer path param (parent [id]) then fallback to query param for backward compatibility
  const novelIdFromPath = (params as any)?.id
  const novelIdParam = novelIdFromPath || searchParams?.get("novelId")

  useEffect(() => {
    fetchNovels()
  }, [])

  useEffect(() => {
    if (novelIdParam && novels.length > 0) {
      const novel = novels.find((n) => n.id === novelIdParam)
      if (novel) {
        setSelectedNovelId(novelIdParam)
        toast({ title: "Novel Pre-selected", description: `Selected "${novel.title}" from URL parameter` })
      }
    }
  }, [novelIdParam, novels, toast])

  const fetchNovels = async () => {
    try {
      setIsLoadingNovels(true)
      const response = await api.getNovels({ size: 100 })
      if (response.success) {
        setNovels(response.data.content)
      }
    } catch (error) {
      console.error("Error fetching novels:", error)
    } finally {
      setIsLoadingNovels(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    // require the appropriate content field based on format
    const missing = !selectedNovelId || !title || !chapterNumber || (format === 'HTML' ? !contentHtml : !contentText)
    if (missing) {
      toast({ title: "Missing Information", description: "Please fill in all required fields", variant: "destructive" })
      return
    }

    try {
  setIsLoading(true)
  const payload: any = { novelId: selectedNovelId, title, chapterNumber: Number.parseInt(chapterNumber), format }
  // backend createChapter expects contentHtml; send HTML or plain text in contentHtml
  if (format === 'HTML') payload.contentHtml = contentHtml
  else payload.contentHtml = contentText

  const response = await api.createChapter(payload)
      if (response.success) {
        toast({ title: "Success", description: "Chapter created successfully" })
        // redirect to novel's chapters listing
        router.push(`/admin/novels/${selectedNovelId}/chapters`)
      } else {
        toast({ title: "Error", description: response.message || "Failed to create chapter", variant: "destructive" })
      }
    } catch (error) {
      console.error("Error creating chapter:", error)
      toast({ title: "Error", description: "Failed to create chapter", variant: "destructive" })
    } finally {
      setIsLoading(false)
    }
  }

  return (
  <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
        <Header />
        <main className="container mx-auto px-4 py-8">
        <div className="space-y-6">
          <div className="flex items-center gap-4">
            <Link href={`/admin/novels/${selectedNovelId || ""}/chapters`}>
              <Button variant="outline" size="sm">Back to Chapters</Button>
            </Link>
            <div>
              <h1 className="text-3xl font-bold">Add Chapter</h1>
              <p className="text-muted-foreground">Create a new chapter</p>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>Chapter Information</CardTitle>
                <CardDescription>Fill in basic details</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <Label>Novel *</Label>
                  <Select value={selectedNovelId} onValueChange={(v) => setSelectedNovelId(v)}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select a novel" />
                    </SelectTrigger>
                    <SelectContent>
                      {novels.map((n) => (
                        <SelectItem key={n.id} value={n.id}>{n.title} - {n.author}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label>Title *</Label>
                    <Input value={title} onChange={(e) => setTitle(e.target.value)} required />
                  </div>
                  <div className="space-y-2">
                    <Label>Chapter Number *</Label>
                    <Input type="number" min="1" value={chapterNumber} onChange={(e) => setChapterNumber(e.target.value)} required />
                  </div>
                </div>

                <div>
                  <div className="flex items-center justify-between">
                    <Label>Content *</Label>
                    <div className="flex items-center gap-2">
                      <Label className="text-sm">Type</Label>
                      <Select value={format} onValueChange={(v) => setFormat(v as 'HTML' | 'TEXT')}>
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
                  {format === 'HTML' ? (
                    <RichTextEditor content={contentHtml} onChange={setContent} />
                  ) : (
                    <Textarea value={contentText} onChange={(e) => setContentText(e.target.value)} rows={20} className="min-h-[400px] font-mono" />
                  )}
                </div>
              </CardContent>
            </Card>

            <div className="flex gap-2">
              <Button type="submit" disabled={isLoading}>{isLoading ? "Creating..." : "Create Chapter"}</Button>
              <Link href="/admin/novels"><Button variant="ghost">Cancel</Button></Link>
            </div>
          </form>
        </div>
      </main>
      </div>
    </AuthGuard>
  )
}
