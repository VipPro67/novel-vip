"use client"

import React, { useEffect, useState } from "react"
import { useParams } from "next/navigation"
import { useRouter } from "@/navigation"
import { Link } from "@/navigation"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { api } from "@/services/api"
import { Loader2, Edit, Plus, Delete } from "lucide-react"
import { se } from "date-fns/locale"
import { Chapter } from "@/models"

export default function ChaptersListPage() {
  const router = useRouter()
  const params = useParams()
  const id = params.id as string

  const [loading, setLoading] = useState(true)
  const [chapters, setChapters] = useState<Chapter[]>([])

  useEffect(() => {
    fetchChapters()
  }, [id])

  const fetchChapters = async () => {
    setLoading(true)
    try {
      const response = await api.getNovelById(id);
      if (!response.success) {
        throw new Error("Novel not found")
      }
      const res = await api.getChaptersByNovel(id, { size: 100, sortBy: "chapterNumber", sortDir: "asc" })
      if (res.success) {
        setChapters(res.data.content)
      }
    } catch (error) {
      console.error("Failed to load chapters", error)
    } finally {
      setLoading(false)
    }
  }

  return (
  <AuthGuard requireRole={["ADMIN","AUTHOR"]}>
      <div className="min-h-screen bg-background">
          <main className="container mx-auto px-4 py-8">
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-bold">Chapters</h1>
                <p className="text-muted-foreground">Manage chapters for this novel</p>
              </div>
              <div className="flex items-center gap-2">
                <Button onClick={() => router.push(`/admin/novels/${id}/chapters/add`)}>
                  <Plus className="h-4 w-4 mr-2" />
                  Add Chapter
                </Button>
                <Button
                  variant="secondary"
                  onClick={async () => {
                    if (window.confirm("Reindex all chapter numbers? This will renumber all chapters sequentially.")) {
                      try {
                        setLoading(true)
                        const res = await api.reindexChapter(id)
                        if (res.success) {
                          alert(`Reindexed chapters. Total chapters: ${res.data}`)
                          fetchChapters()
                        } else {
                          alert(res.message || "Failed to reindex chapters.")
                        }
                      } catch (err) {
                        alert("Failed to reindex chapters.")
                      } finally {
                        setLoading(false)
                      }
                    }
                  }}
                >
                  Reindex Chapters
                </Button>
                <Link href="/admin/novels">
                  <Button variant="outline">Back to Novels</Button>
                </Link>
              </div>
            </div>

            <Card>
              <CardContent>
                {loading ? (
                  <div className="flex items-center justify-center py-12">
                    <Loader2 className="h-6 w-6 animate-spin" />
                  </div>
                ) : chapters.length === 0 ? (
                  <div className="p-8 text-center text-muted-foreground">No chapters found for this novel.</div>
                ) : (
                  <div className="divide-y">
                    {chapters.map((c) => (
                      <div key={c.id} className="p-4 flex items-center justify-between">
                        <div>
                          <div className="text-lg font-medium">{c.chapterNumber}. {c.title}</div>
                        </div>
                        <div className="flex items-center gap-2">
                          <Link href={`/admin/novels/${id}/chapters/${c.chapterNumber}/edit`}>
                            <Button variant="ghost" size="sm">
                              <Edit className="h-4 w-4 mr-2" />
                              Edit
                            </Button>
                            
                          </Link>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={async () => {
                              if (window.confirm(`Are you sure you want to delete chapter ${c.chapterNumber}?`)) {
                                try {
                                  const res = await api.deleteChapter(c.id)
                                      if (res.success) {
                                        setChapters((prev) => prev.filter((ch) => ch.id !== c.id))
                                  } else {
                                    alert(res.message || "Failed to delete chapter.")
                                  }
                                } catch (err) {
                                  alert("Failed to delete chapter.")
                                }
                              }
                            }}
                          >
                            <Delete className="h-4 w-4 mr-2" />
                            Delete
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
