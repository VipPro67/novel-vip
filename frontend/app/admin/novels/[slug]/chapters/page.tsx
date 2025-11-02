"use client"

import React, { useEffect, useState } from "react"
import { useParams, useRouter } from "next/navigation"
import Link from "next/link"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { api, type Chapter, type PageResponse } from "@/lib/api"
import { Loader2, Edit, Plus, Delete } from "lucide-react"
import { se } from "date-fns/locale"

export default function ChaptersListPage() {
  const router = useRouter()
  const params = useParams()
  const slug = params.slug as string

  const [loading, setLoading] = useState(true)
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [novelId, setNovelId] = useState("")

  useEffect(() => {
    fetchChapters()
  }, [slug])

  const fetchChapters = async () => {
    setLoading(true)
    try {
      const response = await api.getNovelBySlug(slug);
      if (!response.success) {
        throw new Error("Novel not found")
      }
      setNovelId(response.data.id)
      const res = await api.getChaptersByNovel(novelId, { size: 100, sortBy: "chapterNumber", sortDir: "asc" })
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
  <AuthGuard requireRole="ADMIN">
      <div className="min-h-screen bg-background">
          <main className="container mx-auto px-4 py-8">
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h1 className="text-3xl font-bold">Chapters</h1>
                <p className="text-muted-foreground">Manage chapters for this novel</p>
              </div>
              <div className="flex items-center gap-2">
                <Button onClick={() => router.push(`/admin/novels/${novelId}/chapters/add`)}>
                  <Plus className="h-4 w-4 mr-2" />
                  Add Chapter
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
                          <Link href={`/admin/novels/${novelId}/chapters/${c.id}/edit`}>
                            <Button variant="ghost" size="sm">
                              <Edit className="h-4 w-4 mr-2" />
                              Edit
                            </Button>
                            
                          </Link>
                          <Button variant="ghost" size="sm">
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
