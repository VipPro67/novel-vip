"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Plus, Search, Filter, MoreHorizontal, Edit, Trash2, Eye, BookOpen, Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { useToast } from "@/hooks/use-toast"
import { api, type Novel, type PageResponse } from "@/lib/api"

export function NovelManagement() {
  const router = useRouter()
  const { toast } = useToast()

  const [novels, setNovels] = useState<Novel[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [novelToDelete, setNovelToDelete] = useState<Novel | null>(null)
  const [deleting, setDeleting] = useState(false)

  // Stats
  const [stats, setStats] = useState({
    total: 0,
    published: 0,
    draft: 0,
    averageRating: 0,
  })

  useEffect(() => {
    fetchNovels()
  }, [currentPage, searchTerm, statusFilter])

  const fetchNovels = async () => {
    setLoading(true)
    try {
      const params: any = {
        page: currentPage,
        size: 10,
        sortBy: "updatedAt",
        sortDir: "desc",
      }

      if (searchTerm.trim()) {
        params.search = searchTerm.trim()
      }

      const response = await api.getNovels(params)

      if (response.success) {
        const data = response.data as PageResponse<Novel>
        let filteredNovels = data.content

        // Apply status filter
        if (statusFilter !== "all") {
          filteredNovels = data.content.filter((novel) => novel.status.toLowerCase() === statusFilter)
        }

        setNovels(filteredNovels)
        setTotalPages(data.totalPages)
        setTotalElements(data.totalElements)

        // Calculate stats
        const total = data.totalElements
        const published = data.content.filter((n) => n.status === "PUBLISHED").length
        const draft = data.content.filter((n) => n.status === "DRAFT").length
        const averageRating = data.content.reduce((acc, n) => acc + n.rating, 0) / data.content.length || 0

        setStats({ total, published, draft, averageRating })
      }
    } catch (error) {
      console.error("Failed to fetch novels:", error)
      toast({
        title: "Error",
        description: "Failed to load novels",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleDeleteNovel = async () => {
    if (!novelToDelete) return

    setDeleting(true)
    try {
      const response = await api.deleteNovel(novelToDelete.id)

      if (response.success) {
        toast({
          title: "Success",
          description: "Novel deleted successfully",
        })
        fetchNovels() // Refresh the list
      }
    } catch (error) {
      console.error("Failed to delete novel:", error)
      toast({
        title: "Error",
        description: "Failed to delete novel",
        variant: "destructive",
      })
    } finally {
      setDeleting(false)
      setDeleteDialogOpen(false)
      setNovelToDelete(null)
    }
  }

  const getStatusBadgeVariant = (status: string) => {
    switch (status.toLowerCase()) {
      case "published":
        return "default"
      case "draft":
        return "secondary"
      case "completed":
        return "outline"
      default:
        return "secondary"
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
    })
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Novel Management</h1>
          <p className="text-muted-foreground">Manage your novels and chapters</p>
        </div>
        <Button onClick={() => router.push("/admin/novels/add")}>
          <Plus className="h-4 w-4 mr-2" />
          Add Novel
        </Button>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Novels</CardTitle>
            <BookOpen className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.total}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Published</CardTitle>
            <Badge variant="default" className="h-4 w-4 rounded-full p-0" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.published}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Drafts</CardTitle>
            <Badge variant="secondary" className="h-4 w-4 rounded-full p-0" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.draft}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Avg Rating</CardTitle>
            <span className="text-yellow-500">★</span>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.averageRating.toFixed(1)}</div>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
          <Input
            placeholder="Search novels..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            <Filter className="h-4 w-4 mr-2" />
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Status</SelectItem>
            <SelectItem value="published">Published</SelectItem>
            <SelectItem value="draft">Draft</SelectItem>
            <SelectItem value="completed">Completed</SelectItem>
            <SelectItem value="ongoing">Ongoing</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Novels List */}
      <Card>
        <CardContent className="p-0">
          {loading ? (
            <div className="p-8">
              <div className="space-y-4">
                {Array.from({ length: 5 }).map((_, i) => (
                  <div key={i} className="flex items-center space-x-4 animate-pulse">
                    <div className="h-16 w-12 bg-muted rounded"></div>
                    <div className="flex-1 space-y-2">
                      <div className="h-4 bg-muted rounded w-1/3"></div>
                      <div className="h-3 bg-muted rounded w-1/2"></div>
                      <div className="h-3 bg-muted rounded w-1/4"></div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ) : novels.length === 0 ? (
            <div className="p-8 text-center">
              <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">No novels found</h3>
              <p className="text-muted-foreground mb-4">
                {searchTerm ? "No novels match your search criteria." : "Get started by creating your first novel."}
              </p>
              <Button onClick={() => router.push("/admin/novels/add")}>
                <Plus className="h-4 w-4 mr-2" />
                Add Novel
              </Button>
            </div>
          ) : (
            <div className="divide-y">
              {novels.map((novel) => (
                <div key={novel.id} className="p-6 flex items-center justify-between hover:bg-muted/50">
                  <div className="flex items-center space-x-4">
                    <Avatar className="h-16 w-12 rounded-md">
                      <AvatarImage
                        src={novel.coverImage?.fileUrl || "/placeholder.jpg"}
                        alt={novel.title}
                        className="object-cover"
                      />
                      <AvatarFallback className="rounded-md">{novel.title.charAt(0)}</AvatarFallback>
                    </Avatar>
                    <div className="space-y-1">
                      <h3 className="font-semibold text-lg">{novel.title}</h3>
                      <p className="text-sm text-muted-foreground">by {novel.author}</p>
                      <div className="flex items-center space-x-4 text-sm text-muted-foreground">
                        <span>{novel.totalChapters} chapters</span>
                        <span>{novel.views.toLocaleString()} views</span>
                        <span className="flex items-center">
                          <span className="text-yellow-500 mr-1">★</span>
                          {novel.rating.toFixed(1)}
                        </span>
                        <span>Updated {formatDate(novel.updatedAt)}</span>
                      </div>
                      <div className="flex items-center space-x-2">
                        <Badge variant={getStatusBadgeVariant(novel.status)}>{novel.status}</Badge>
                        {novel.categories.map((category) => (
                          <Badge key={category.id} variant="outline" className="text-xs">
                            {category.name}
                          </Badge>
                        ))}
                      </div>
                    </div>
                  </div>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="sm">
                        <MoreHorizontal className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onClick={() => router.push(`/novels/${novel.id}`)}>
                        <Eye className="h-4 w-4 mr-2" />
                        View Details
                      </DropdownMenuItem>
                      <DropdownMenuItem onClick={() => router.push(`/admin/novels/${novel.id}/edit`)}>
                        <Edit className="h-4 w-4 mr-2" />
                        Edit Novel
                      </DropdownMenuItem>
                      <DropdownMenuItem onClick={() => router.push(`/admin/chapters/add?novelId=${novel.id}`)}>
                        <Plus className="h-4 w-4 mr-2" />
                        Add Chapter
                      </DropdownMenuItem>
                      <DropdownMenuItem
                        onClick={() => {
                          setNovelToDelete(novel)
                          setDeleteDialogOpen(true)
                        }}
                        className="text-destructive"
                      >
                        <Trash2 className="h-4 w-4 mr-2" />
                        Delete Novel
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center space-x-2">
          <Button
            variant="outline"
            onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
            disabled={currentPage === 0}
          >
            Previous
          </Button>
          <span className="flex items-center px-4 text-sm text-muted-foreground">
            Page {currentPage + 1} of {totalPages}
          </span>
          <Button
            variant="outline"
            onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
            disabled={currentPage === totalPages - 1}
          >
            Next
          </Button>
        </div>
      )}

      {/* Delete Confirmation Dialog */}
      <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Delete Novel</AlertDialogTitle>
            <AlertDialogDescription>
              Are you sure you want to delete "{novelToDelete?.title}"? This action cannot be undone and will also
              delete all associated chapters.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDeleteNovel}
              disabled={deleting}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
            >
              {deleting ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin mr-2" />
                  Deleting...
                </>
              ) : (
                "Delete Novel"
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
