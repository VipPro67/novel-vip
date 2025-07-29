"use client"

import { useState, useEffect } from "react"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Search, MoreHorizontal, Edit, Trash2, Eye, Plus } from "lucide-react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Pagination } from "@/components/ui/pagination"
import Link from "next/link"
import { useToast } from "@/hooks/use-toast"

interface Chapter {
  id: string
  title: string
  chapterNumber: number
  novelTitle: string
  novelId: string
  wordCount: number
  publishedAt: string
  status: "published" | "draft" | "scheduled"
  views: number
}

export default function AdminChaptersPage() {
  const [chapters, setChapters] = useState<Chapter[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const { toast } = useToast()

  useEffect(() => {
    const timeoutId = setTimeout(
      () => {
        setCurrentPage(0)
        fetchChapters()
      },
      searchQuery ? 500 : 0,
    )

    return () => clearTimeout(timeoutId)
  }, [currentPage, searchQuery])

  const fetchChapters = async () => {
    setLoading(true)
    try {
      // Mock data - replace with actual API call
      const mockChapters: Chapter[] = [
        {
          id: "1",
          title: "The Beginning of Everything",
          chapterNumber: 1,
          novelTitle: "Epic Fantasy Adventure",
          novelId: "novel-1",
          wordCount: 2500,
          publishedAt: "2024-01-15",
          status: "published",
          views: 1250,
        },
        {
          id: "2",
          title: "The Journey Continues",
          chapterNumber: 2,
          novelTitle: "Epic Fantasy Adventure",
          novelId: "novel-1",
          wordCount: 2800,
          publishedAt: "2024-01-16",
          status: "published",
          views: 980,
        },
        {
          id: "3",
          title: "New Challenges Arise",
          chapterNumber: 3,
          novelTitle: "Epic Fantasy Adventure",
          novelId: "novel-1",
          wordCount: 3200,
          publishedAt: "2024-01-17",
          status: "draft",
          views: 0,
        },
      ]

      setChapters(mockChapters)
      setTotalPages(1)
    } catch (error) {
      console.error("Failed to fetch chapters:", error)
      toast({
        title: "Error",
        description: "Failed to fetch chapters",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "published":
        return "bg-green-100 text-green-800"
      case "draft":
        return "bg-yellow-100 text-yellow-800"
      case "scheduled":
        return "bg-blue-100 text-blue-800"
      default:
        return "bg-gray-100 text-gray-800"
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
              <Link href="/admin">
                <Button variant="outline" size="sm">
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back to Dashboard
                </Button>
              </Link>
              <div>
                <h1 className="text-3xl font-bold">Chapter Management</h1>
                <p className="text-muted-foreground">Manage chapters across all novels</p>
              </div>
            </div>

            {/* Chapter Management */}
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Chapter Management</CardTitle>
                    <CardDescription>Manage chapters and their content</CardDescription>
                  </div>
                  <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    Add Chapter
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {/* Search */}
                  <div className="flex items-center space-x-2">
                    <div className="relative flex-1 max-w-sm">
                      <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                      <Input
                        placeholder="Search chapters by title or novel..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="pl-8"
                      />
                    </div>
                    {searchQuery && (
                      <Button variant="outline" size="sm" onClick={() => setSearchQuery("")}>
                        Clear
                      </Button>
                    )}
                  </div>

                  {/* Chapters Table */}
                  <div className="border rounded-md">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Chapter</TableHead>
                          <TableHead>Novel</TableHead>
                          <TableHead>Status</TableHead>
                          <TableHead>Word Count</TableHead>
                          <TableHead>Views</TableHead>
                          <TableHead>Published</TableHead>
                          <TableHead className="w-[70px]">Actions</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {loading ? (
                          Array.from({ length: 5 }).map((_, i) => (
                            <TableRow key={i}>
                              <TableCell>
                                <div className="space-y-1">
                                  <div className="h-4 w-32 bg-muted rounded animate-pulse" />
                                  <div className="h-3 w-16 bg-muted rounded animate-pulse" />
                                </div>
                              </TableCell>
                              <TableCell>
                                <div className="h-4 w-24 bg-muted rounded animate-pulse" />
                              </TableCell>
                              <TableCell>
                                <div className="h-4 w-16 bg-muted rounded animate-pulse" />
                              </TableCell>
                              <TableCell>
                                <div className="h-4 w-12 bg-muted rounded animate-pulse" />
                              </TableCell>
                              <TableCell>
                                <div className="h-4 w-12 bg-muted rounded animate-pulse" />
                              </TableCell>
                              <TableCell>
                                <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                              </TableCell>
                              <TableCell>
                                <div className="h-4 w-8 bg-muted rounded animate-pulse" />
                              </TableCell>
                            </TableRow>
                          ))
                        ) : chapters.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={7} className="text-center py-8">
                              No chapters found
                            </TableCell>
                          </TableRow>
                        ) : (
                          chapters.map((chapter) => (
                            <TableRow key={chapter.id}>
                              <TableCell>
                                <div>
                                  <p className="font-medium line-clamp-1">{chapter.title}</p>
                                  <p className="text-sm text-muted-foreground">Chapter {chapter.chapterNumber}</p>
                                </div>
                              </TableCell>
                              <TableCell>
                                <Link href={`/novels/${chapter.novelId}`} className="text-blue-600 hover:underline">
                                  {chapter.novelTitle}
                                </Link>
                              </TableCell>
                              <TableCell>
                                <Badge className={getStatusColor(chapter.status)}>{chapter.status}</Badge>
                              </TableCell>
                              <TableCell>{chapter.wordCount.toLocaleString()}</TableCell>
                              <TableCell>{chapter.views.toLocaleString()}</TableCell>
                              <TableCell>
                                <span className="text-sm text-muted-foreground">
                                  {new Date(chapter.publishedAt).toLocaleDateString()}
                                </span>
                              </TableCell>
                              <TableCell>
                                <DropdownMenu>
                                  <DropdownMenuTrigger asChild>
                                    <Button variant="ghost" size="icon">
                                      <MoreHorizontal className="h-4 w-4" />
                                    </Button>
                                  </DropdownMenuTrigger>
                                  <DropdownMenuContent align="end">
                                    <DropdownMenuItem>
                                      <Eye className="mr-2 h-4 w-4" />
                                      View Chapter
                                    </DropdownMenuItem>
                                    <DropdownMenuItem>
                                      <Edit className="mr-2 h-4 w-4" />
                                      Edit Chapter
                                    </DropdownMenuItem>
                                    <DropdownMenuSeparator />
                                    <DropdownMenuItem className="text-destructive">
                                      <Trash2 className="mr-2 h-4 w-4" />
                                      Delete Chapter
                                    </DropdownMenuItem>
                                  </DropdownMenuContent>
                                </DropdownMenu>
                              </TableCell>
                            </TableRow>
                          ))
                        )}
                      </TableBody>
                    </Table>
                  </div>

                  {/* Pagination */}
                  <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={setCurrentPage}
                    showPageNumbers={true}
                    className="mt-4"
                  />
                </div>
              </CardContent>
            </Card>
          </div>
        </main>
      </div>
    </AuthGuard>
  )
}
