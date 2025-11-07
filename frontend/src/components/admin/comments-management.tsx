"use client"

import { useState, useEffect } from "react"
import { Trash2, Eye, Check, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { useToast } from "@/hooks/use-toast"
import { api, type Comment } from "@/services/api"
import { Pagination } from "@/components/ui/pagination"
import { usePagination } from "@/hooks/use-pagination"

export function CommentsManagement() {
  const [comments, setComments] = useState<Comment[]>([])
  const [loading, setLoading] = useState(true)
  const { currentPage, totalPages, handlePageChange, updateTotalPages, resetPage, getPaginationParams } = usePagination(
    {
      initialPage: 0,
      initialSize: 10,
      initialSortBy: "createdAt",
      initialSortDir: "desc",
    },
  )
  const [selectedComment, setSelectedComment] = useState<Comment | null>(null)
  const [viewDialog, setViewDialog] = useState(false)
  const [deleteDialog, setDeleteDialog] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    fetchComments()
  }, [currentPage, getPaginationParams])

  const fetchComments = async () => {
    setLoading(true)
    try {
      const paginationParams = getPaginationParams()
      const response = await api.getAllComments(paginationParams)
      if (response.success) {
        setComments(response.data.content)
        updateTotalPages(response.data.totalPages)
      }
    } catch (error) {
      console.error("Failed to fetch comments:", error)
      toast({
        title: "Error",
        description: "Failed to fetch comments",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (comment: Comment) => {
    try {
      await api.approveComment(comment.id)
      toast({
        title: "Success",
        description: "Comment approved",
      })
      fetchComments()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to approve comment",
        variant: "destructive",
      })
    }
  }

  const handleReject = async (comment: Comment) => {
    try {
      await api.rejectComment(comment.id)
      toast({
        title: "Success",
        description: "Comment rejected",
      })
      fetchComments()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to reject comment",
        variant: "destructive",
      })
    }
  }

  const handleDelete = async () => {
    if (!selectedComment) return
    try {
      await api.deleteComment(selectedComment.id)
      toast({
        title: "Success",
        description: "Comment deleted",
      })
      setDeleteDialog(false)
      fetchComments()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete comment",
        variant: "destructive",
      })
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Comments Management</CardTitle>
        <CardDescription>Moderate and manage user comments</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="border rounded-md">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Author</TableHead>
                  <TableHead>Content</TableHead>
                  <TableHead>Location</TableHead>
                  <TableHead>Date</TableHead>
                  <TableHead className="w-[70px]">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  Array.from({ length: 5 }).map((_, i) => (
                    <TableRow key={i}>
                      <TableCell>
                        <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-40 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-24 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-8 bg-muted rounded animate-pulse" />
                      </TableCell>
                    </TableRow>
                  ))
                ) : comments.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center py-8">
                      No comments found
                    </TableCell>
                  </TableRow>
                ) : (
                  comments.map((comment) => (
                    <TableRow key={comment.id}>
                      <TableCell className="font-medium">@{comment.username}</TableCell>
                      <TableCell className="max-w-xs truncate text-sm">{comment.content}</TableCell>
                      <TableCell className="text-sm text-muted-foreground">
                        {comment.novelId ? "Novel" : "Chapter"}
                      </TableCell>
                      <TableCell className="text-sm">{new Date(comment.createdAt).toLocaleDateString()}</TableCell>
                      <TableCell>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon">
                              <Eye className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem
                              onClick={() => {
                                setSelectedComment(comment)
                                setViewDialog(true)
                              }}
                            >
                              View Details
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => handleApprove(comment)}>
                              <Check className="mr-2 h-4 w-4" />
                              Approve
                            </DropdownMenuItem>
                            <DropdownMenuItem onClick={() => handleReject(comment)}>
                              <X className="mr-2 h-4 w-4" />
                              Reject
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => {
                                setSelectedComment(comment)
                                setDeleteDialog(true)
                              }}
                              className="text-destructive"
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              Delete
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

          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
            showPageNumbers={true}
            className="mt-4"
          />
        </div>
      </CardContent>

      <Dialog open={viewDialog} onOpenChange={setViewDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Comment Details</DialogTitle>
          </DialogHeader>
          {selectedComment && (
            <div className="space-y-4">
              <div>
                <p className="text-sm font-medium">Author</p>
                <p className="text-sm text-muted-foreground">@{selectedComment.username}</p>
              </div>
              <div>
                <p className="text-sm font-medium">Content</p>
                <p className="text-sm text-muted-foreground mt-1">{selectedComment.content}</p>
              </div>
              <div>
                <p className="text-sm font-medium">Date</p>
                <p className="text-sm text-muted-foreground">{new Date(selectedComment.createdAt).toLocaleString()}</p>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setViewDialog(false)}>
              Close
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={deleteDialog} onOpenChange={setDeleteDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Comment</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this comment? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDialog(false)}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleDelete}>
              Delete Comment
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Card>
  )
}
