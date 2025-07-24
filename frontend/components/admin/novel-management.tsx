"use client"

import { useState, useEffect } from "react"
import { MoreHorizontal, Eye, Edit, Trash2, Search, Plus } from "lucide-react"
import Image from "next/image"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { useToast } from "@/hooks/use-toast"
import { api, type Novel } from "@/lib/api"
// Import the new Pagination component
import { Pagination } from "@/components/ui/pagination"

export function NovelManagement() {
  const [novels, setNovels] = useState<Novel[]>([])
  const [loading, setLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const { toast } = useToast()

  // Update the useEffect to include debounced search
  useEffect(() => {
    const timeoutId = setTimeout(
      () => {
        setCurrentPage(0) // Reset to first page when searching
        fetchNovels()
      },
      searchQuery ? 500 : 0,
    ) // 500ms debounce for search, immediate for page changes

    return () => clearTimeout(timeoutId)
  }, [currentPage, searchQuery])

  // Update the fetchNovels function to use proper pagination
  const fetchNovels = async () => {
    setLoading(true)
    try {
      const response = await api.getNovels({
        page: currentPage,
        size: 10,
        sortBy: "updatedAt",
        sortDir: "desc",
        search: searchQuery.trim() || undefined,
      })

      console.log("Novels API response:", response)

      if (response.success) {
        setNovels(response.data.content)
        setTotalPages(response.data.totalPages)
      }
    } catch (error) {
      console.error("Failed to fetch novels:", error)
      toast({
        title: "Error",
        description: "Failed to fetch novels",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case "completed":
        return "bg-green-100 text-green-800"
      case "ongoing":
        return "bg-blue-100 text-blue-800"
      case "hiatus":
        return "bg-yellow-100 text-yellow-800"
      case "dropped":
        return "bg-red-100 text-red-800"
      default:
        return "bg-gray-100 text-gray-800"
    }
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Novel Management</CardTitle>
            <CardDescription>Manage novels and their content</CardDescription>
          </div>
          <Button>
            <Plus className="mr-2 h-4 w-4" />
            Add Novel
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {/* Search */}
          {/* Update the search input */}
          <div className="flex items-center space-x-2">
            <div className="relative flex-1 max-w-sm">
              <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="Search novels by title or author..."
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

          {/* Novels Table */}
          <div className="border rounded-md">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Novel</TableHead>
                  <TableHead>Author</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Chapters</TableHead>
                  <TableHead>Views</TableHead>
                  <TableHead>Rating</TableHead>
                  <TableHead className="w-[70px]">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  Array.from({ length: 5 }).map((_, i) => (
                    <TableRow key={i}>
                      <TableCell>
                        <div className="flex items-center space-x-3">
                          <div className="h-12 w-9 bg-muted rounded animate-pulse" />
                          <div className="space-y-1">
                            <div className="h-4 w-32 bg-muted rounded animate-pulse" />
                            <div className="h-3 w-24 bg-muted rounded animate-pulse" />
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-16 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-12 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-16 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-12 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-8 bg-muted rounded animate-pulse" />
                      </TableCell>
                    </TableRow>
                  ))
                ) : novels.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center py-8">
                      No novels found
                    </TableCell>
                  </TableRow>
                ) : (
                  novels.map((novel) => (
                    <TableRow key={novel.id}>
                      <TableCell>
                        <div className="flex items-center space-x-3">
                          <div className="relative h-12 w-9">
                            <Image
                              src={novel.coverImage || "/placeholder.svg?height=48&width=36"}
                              alt={novel.title}
                              fill
                              className="object-cover rounded"
                            />
                          </div>
                          <div>
                            <p className="font-medium line-clamp-1">{novel.title}</p>
                            <p className="text-sm text-muted-foreground line-clamp-1">{novel.description}</p>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>{novel.author}</TableCell>
                      <TableCell>
                        <Badge className={getStatusColor(novel.status)}>{novel.status}</Badge>
                      </TableCell>
                      <TableCell>{novel.totalChapters}</TableCell>
                      <TableCell>{novel.views.toLocaleString()}</TableCell>
                      <TableCell>
                        <div className="flex items-center space-x-1">
                          <span>{novel.rating}</span>
                          <span className="text-muted-foreground">/5</span>
                        </div>
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
                              View Details
                            </DropdownMenuItem>
                            <DropdownMenuItem>
                              <Edit className="mr-2 h-4 w-4" />
                              Edit Novel
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem className="text-destructive">
                              <Trash2 className="mr-2 h-4 w-4" />
                              Delete Novel
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
  )
}
