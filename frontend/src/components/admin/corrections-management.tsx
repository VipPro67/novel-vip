"use client"

import { useState, useEffect } from "react"
import { Check, X, Eye, Search, Filter } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Textarea } from "@/components/ui/textarea"
import { useToast } from "@/hooks/use-toast"
import { api, type CorrectionRequest } from "@/services/api"
import { Pagination } from "@/components/ui/pagination"
import { usePagination } from "@/hooks/use-pagination"

export function CorrectionsManagement() {
  const [corrections, setCorrections] = useState<CorrectionRequest[]>([])
  const [loading, setLoading] = useState(true)
  const { currentPage, totalPages, handlePageChange, updateTotalPages, resetPage, getPaginationParams } = usePagination(
    {
      initialPage: 0,
      initialSize: 10,
      initialSortBy: "createdAt",
      initialSortDir: "desc",
    },
  )
  const [selectedCorrection, setSelectedCorrection] = useState<CorrectionRequest | null>(null)
  const [viewDialog, setViewDialog] = useState(false)
  const [rejectDialog, setRejectDialog] = useState(false)
  const [rejectionReason, setRejectionReason] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("PENDING")
  const [searchQuery, setSearchQuery] = useState("")
  const [chapterContent, setChapterContent] = useState<string>("")
  const [loadingContent, setLoadingContent] = useState(false)
  const { toast } = useToast()

  useEffect(() => {
    fetchCorrections()
  }, [currentPage, statusFilter, getPaginationParams])

  const fetchCorrections = async () => {
    setLoading(true)
    try {
      let response
      if (statusFilter === "PENDING") {
        const paginationParams = getPaginationParams()
        response = await api.getPendingCorrections({
          page: paginationParams.page,
          size: paginationParams.size,
          direction: paginationParams.sortDir.toUpperCase() as "ASC" | "DESC",
        })
      } else {
        response = await api.getCorrectionsByStatus(statusFilter, {
          page: currentPage,
          size: 10,
        })
      }
      if (response.success) {
        setCorrections(response.data.content)
        updateTotalPages(response.data.totalPages)
      }
    } catch (error) {
      console.error("Failed to fetch corrections:", error)
      toast({
        title: "Error",
        description: "Failed to fetch corrections",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (correction: CorrectionRequest) => {
    try {
      await api.approveCorrection(correction.id)
      toast({
        title: "Success",
        description: "Correction approved and applied",
      })
      fetchCorrections()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to approve correction",
        variant: "destructive",
      })
    }
  }

  const handleReject = async () => {
    if (!selectedCorrection) return
    try {
      await api.rejectCorrection(selectedCorrection.id, rejectionReason || undefined)
      toast({
        title: "Success",
        description: "Correction rejected",
      })
      setRejectDialog(false)
      setRejectionReason("")
      setSelectedCorrection(null)
      fetchCorrections()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to reject correction",
        variant: "destructive",
      })
    }
  }

  const handleViewCorrection = async (correction: CorrectionRequest) => {
    setSelectedCorrection(correction)
    setViewDialog(true)
    setLoadingContent(true)
    try {
      // Fetch chapter details first
      const chapterResponse = await api.getChapterById(correction.chapterId)
      if (chapterResponse.success && chapterResponse.data.jsonUrl) {
        // Fetch content from S3
        const contentResponse = await fetch(chapterResponse.data.jsonUrl)
        if (contentResponse.ok) {
          const contentData = await contentResponse.json()
          setChapterContent(contentData.content || "")
        } else {
          setChapterContent("")
        }
      } else {
        setChapterContent("")
      }
    } catch (error) {
      console.error("Failed to fetch chapter content:", error)
      setChapterContent("")
    } finally {
      setLoadingContent(false)
    }
  }

  const renderTextWithCorrection = (content: string, correction: CorrectionRequest) => {
    if (!correction.charIndex) return content.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')

    const before = content.slice(0, correction.charIndex).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    const original = correction.originalText.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    const suggested = correction.suggestedText.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    const after = content.slice(correction.charIndex + correction.originalText.length).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')

    return `${before}<span class="bg-red-200 dark:bg-red-900 px-1 rounded">${original}</span><span class="bg-green-200 dark:bg-green-900 px-1 rounded ml-1">${suggested}</span>${after}`
  }

  const getStatusBadge = (status: string) => {
    switch (status) {
      case "PENDING":
        return <Badge variant="secondary">Pending</Badge>
      case "APPROVED":
        return <Badge variant="default">Approved</Badge>
      case "REJECTED":
        return <Badge variant="destructive">Rejected</Badge>
      default:
        return <Badge variant="outline">{status}</Badge>
    }
  }

  const filteredCorrections = corrections.filter(correction =>
    searchQuery === "" ||
    correction.originalText.toLowerCase().includes(searchQuery.toLowerCase()) ||
    correction.suggestedText.toLowerCase().includes(searchQuery.toLowerCase()) ||
    correction.reason?.toLowerCase().includes(searchQuery.toLowerCase())
  )

  return (
    <div className="space-y-6">
      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle>Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search corrections..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-full sm:w-48">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="PENDING">Pending</SelectItem>
                <SelectItem value="APPROVED">Approved</SelectItem>
                <SelectItem value="REJECTED">Rejected</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Corrections Table */}
      <Card>
        <CardHeader>
          <CardTitle>Correction Requests</CardTitle>
          <CardDescription>
            Review and manage text correction requests from users
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
            </div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Novel</TableHead>
                    <TableHead>Chapter</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredCorrections.map((correction) => (
                    <TableRow key={correction.id}>
                      <TableCell className="font-medium">
                        Novel #{correction.novelId.slice(0, 8)}...
                      </TableCell>
                      <TableCell>Chapter {correction.chapterNumber}</TableCell>
                      <TableCell>{getStatusBadge(correction.status)}</TableCell>
                      <TableCell>
                        {new Date(correction.createdAt).toLocaleDateString()}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleViewCorrection(correction)}
                          >
                            <Eye className="h-4 w-4" />
                          </Button>
                          {correction.status === "PENDING" && (
                            <>
                              <Button
                                size="sm"
                                variant="default"
                                onClick={() => handleApprove(correction)}
                              >
                                <Check className="h-4 w-4" />
                              </Button>
                              <Button
                                size="sm"
                                variant="destructive"
                                onClick={() => {
                                  setSelectedCorrection(correction)
                                  setRejectDialog(true)
                                }}
                              >
                                <X className="h-4 w-4" />
                              </Button>
                            </>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
              {totalPages > 1 && (
                <div className="mt-4">
                  <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                  />
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {/* View Correction Dialog */}
      <Dialog open={viewDialog} onOpenChange={(open) => {
        setViewDialog(open)
        if (!open) {
          setChapterContent("")
          setSelectedCorrection(null)
        }
      }}>
        <DialogContent className="max-w-6xl max-h-[80vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Correction Details</DialogTitle>
            <DialogDescription>
              Review the proposed text correction in context
            </DialogDescription>
          </DialogHeader>
          {selectedCorrection && (
            <div className="space-y-6">
              {/* User's Reason */}
              {selectedCorrection.reason && (
                <div>
                  <h4 className="font-semibold mb-2">User's Reason</h4>
                  <div className="p-3 bg-blue-50 dark:bg-blue-950/20 border border-blue-200 dark:border-blue-800 rounded-md">
                    <p className="text-sm">{selectedCorrection.reason}</p>
                  </div>
                </div>
              )}

              {/* Context with Correction */}
              <div>
                <h4 className="font-semibold mb-2">Correction in Context</h4>
                {loadingContent ? (
                  <div className="flex items-center justify-center py-8">
                    <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-primary"></div>
                  </div>
                ) : chapterContent ? (
                  <div className="p-4 bg-gray-50 dark:bg-gray-900 border rounded-md max-h-96 overflow-y-auto">
                    <div
                      className="text-sm leading-relaxed whitespace-pre-wrap"
                      dangerouslySetInnerHTML={{
                        __html: renderTextWithCorrection(chapterContent, selectedCorrection)
                      }}
                    />
                  </div>
                ) : (
                  <div className="p-4 bg-gray-50 dark:bg-gray-900 border rounded-md">
                    <p className="text-sm text-muted-foreground">Unable to load chapter content</p>
                  </div>
                )}
              </div>

              {/* Original vs Suggested */}
              <div>
                <h4 className="font-semibold mb-2">Text Changes</h4>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <h5 className="text-sm font-medium mb-2 text-red-600">Original Text</h5>
                    <div className="p-3 bg-red-50 dark:bg-red-950/20 border border-red-200 dark:border-red-800 rounded-md">
                      <pre className="whitespace-pre-wrap text-sm">{selectedCorrection.originalText}</pre>
                    </div>
                  </div>
                  <div>
                    <h5 className="text-sm font-medium mb-2 text-green-600">Suggested Text</h5>
                    <div className="p-3 bg-green-50 dark:bg-green-950/20 border border-green-200 dark:border-green-800 rounded-md">
                      <pre className="whitespace-pre-wrap text-sm">{selectedCorrection.suggestedText}</pre>
                    </div>
                  </div>
                </div>
              </div>

              {/* Metadata */}
              <div className="text-sm text-muted-foreground grid grid-cols-2 gap-4">
                <div>
                  <p><strong>Novel ID:</strong> {selectedCorrection.novelId}</p>
                  <p><strong>Chapter:</strong> {selectedCorrection.chapterNumber}</p>
                  <p><strong>Character Index:</strong> {selectedCorrection.charIndex || 'N/A'}</p>
                </div>
                <div>
                  <p><strong>Status:</strong> {getStatusBadge(selectedCorrection.status)}</p>
                  <p><strong>Submitted:</strong> {new Date(selectedCorrection.createdAt).toLocaleString()}</p>
                  {selectedCorrection.rejectionReason && (
                    <p><strong>Rejection Reason:</strong> {selectedCorrection.rejectionReason}</p>
                  )}
                </div>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Reject Correction Dialog */}
      <Dialog open={rejectDialog} onOpenChange={setRejectDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reject Correction</DialogTitle>
            <DialogDescription>
              Provide a reason for rejecting this correction request
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <Textarea
              placeholder="Reason for rejection (optional)"
              value={rejectionReason}
              onChange={(e) => setRejectionReason(e.target.value)}
            />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRejectDialog(false)}>
              Cancel
            </Button>
            <Button variant="destructive" onClick={handleReject}>
              Reject
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}