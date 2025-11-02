"use client"

import { useState, useEffect } from "react"
import { Trash2, Eye, Flag, Star } from "lucide-react"
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
import { api, type Review } from "@/lib/api"
import { Pagination } from "@/components/ui/pagination"
import { usePagination } from "@/hooks/use-pagination"

export function ReviewsManagement() {
  const [reviews, setReviews] = useState<Review[]>([])
  const [loading, setLoading] = useState(true)
  const { currentPage, totalPages, handlePageChange, updateTotalPages, resetPage, getPaginationParams } = usePagination(
    {
      initialPage: 0,
      initialSize: 10,
      initialSortBy: "createdAt",
      initialSortDir: "desc",
    },
  )
  const [selectedReview, setSelectedReview] = useState<Review | null>(null)
  const [viewDialog, setViewDialog] = useState(false)
  const [deleteDialog, setDeleteDialog] = useState(false)
  const [flagDialog, setFlagDialog] = useState(false)
  const [flagReason, setFlagReason] = useState("")
  const { toast } = useToast()

  useEffect(() => {
    fetchReviews()
  }, [currentPage, getPaginationParams])

  const fetchReviews = async () => {
    setLoading(true)
    try {
      const paginationParams = getPaginationParams()
      const response = await api.getAllReviews(paginationParams)
      if (response.success) {
        setReviews(response.data.content)
        updateTotalPages(response.data.totalPages)
      }
    } catch (error) {
      console.error("Failed to fetch reviews:", error)
      toast({
        title: "Error",
        description: "Failed to fetch reviews",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleFlag = async () => {
    if (!selectedReview || !flagReason.trim()) return
    try {
      await api.flagReview(selectedReview.id, flagReason)
      toast({
        title: "Success",
        description: "Review flagged for review",
      })
      setFlagDialog(false)
      setFlagReason("")
      fetchReviews()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to flag review",
        variant: "destructive",
      })
    }
  }

  const handleDelete = async () => {
    if (!selectedReview) return
    try {
      await api.deleteReview(selectedReview.id)
      toast({
        title: "Success",
        description: "Review deleted",
      })
      setDeleteDialog(false)
      fetchReviews()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete review",
        variant: "destructive",
      })
    }
  }

  const renderStars = (rating: number) => {
    return (
      <div className="flex items-center gap-1">
        {Array.from({ length: 5 }).map((_, i) => (
          <Star
            key={i}
            className={`h-4 w-4 ${i < rating ? "fill-yellow-400 text-yellow-400" : "text-muted-foreground"}`}
          />
        ))}
      </div>
    )
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Reviews Management</CardTitle>
        <CardDescription>Moderate novel reviews and ratings</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="border rounded-md">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Novel</TableHead>
                  <TableHead>Reviewer</TableHead>
                  <TableHead>Rating</TableHead>
                  <TableHead>Title</TableHead>
                  <TableHead>Date</TableHead>
                  <TableHead className="w-[70px]">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  Array.from({ length: 5 }).map((_, i) => (
                    <TableRow key={i}>
                      <TableCell>
                        <div className="h-4 w-24 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-16 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-28 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-8 bg-muted rounded animate-pulse" />
                      </TableCell>
                    </TableRow>
                  ))
                ) : reviews.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center py-8">
                      No reviews found
                    </TableCell>
                  </TableRow>
                ) : (
                  reviews.map((review) => (
                    <TableRow key={review.id}>
                      <TableCell className="font-medium">{review.novelTitle}</TableCell>
                      <TableCell className="text-sm">@{review.username}</TableCell>
                      <TableCell>{renderStars(review.rating)}</TableCell>
                      <TableCell className="text-sm max-w-xs truncate">{review.title}</TableCell>
                      <TableCell className="text-sm">{new Date(review.createdAt).toLocaleDateString()}</TableCell>
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
                                setSelectedReview(review)
                                setViewDialog(true)
                              }}
                            >
                              View Details
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => {
                                setSelectedReview(review)
                                setFlagDialog(true)
                              }}
                            >
                              <Flag className="mr-2 h-4 w-4" />
                              Flag Review
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => {
                                setSelectedReview(review)
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
            <DialogTitle>Review Details</DialogTitle>
          </DialogHeader>
          {selectedReview && (
            <div className="space-y-4">
              <div>
                <p className="text-sm font-medium">Novel</p>
                <p className="text-sm text-muted-foreground">{selectedReview.novelTitle}</p>
              </div>
              <div>
                <p className="text-sm font-medium">Rating</p>
                <div className="text-sm mt-1">{renderStars(selectedReview.rating)}</div>
              </div>
              <div>
                <p className="text-sm font-medium">Title</p>
                <p className="text-sm text-muted-foreground">{selectedReview.title}</p>
              </div>
              <div>
                <p className="text-sm font-medium">Review</p>
                <p className="text-sm text-muted-foreground mt-1">{selectedReview.content}</p>
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
            <DialogTitle>Delete Review</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this review? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDialog(false)}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleDelete}>
              Delete Review
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={flagDialog} onOpenChange={setFlagDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Flag Review</DialogTitle>
            <DialogDescription>Indicate why this review should be flagged</DialogDescription>
          </DialogHeader>
          <textarea
            className="w-full p-2 border rounded text-sm"
            placeholder="Reason for flagging..."
            value={flagReason}
            onChange={(e) => setFlagReason(e.target.value)}
            rows={4}
          />
          <DialogFooter>
            <Button variant="outline" onClick={() => setFlagDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleFlag}>Flag Review</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Card>
  )
}
