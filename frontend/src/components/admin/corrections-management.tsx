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
import { api, type CorrectionRequestWithDetails } from "@/services/api"
import { Pagination } from "@/components/ui/pagination"
import { usePagination } from "@/hooks/use-pagination"

export function CorrectionsManagement() {
  const [corrections, setCorrections] = useState<CorrectionRequestWithDetails[]>([])
  const [loading, setLoading] = useState(true)
  const { currentPage, totalPages, handlePageChange, updateTotalPages, resetPage, getPaginationParams } = usePagination(
    {
      initialPage: 0,
      initialSize: 10,
      initialSortBy: "createdAt",
      initialSortDir: "desc",
    },
  )
  const [selectedCorrection, setSelectedCorrection] = useState<CorrectionRequestWithDetails | null>(null)
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

  const handleApprove = async (correction: CorrectionRequestWithDetails) => {
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

  const handleViewCorrection = async (correction: CorrectionRequestWithDetails) => {
    setSelectedCorrection(correction)
    setViewDialog(true)
    setLoadingContent(false) // No need to load content since we have context saved

    // Build context content from saved paragraphs
    let contextContent = ""
    if (correction.previousParagraph) {
      contextContent += correction.previousParagraph + '\n\n...\n\n'
    }
    // Add the paragraph with the correction (we'll highlight it in renderTextWithCorrection)
    // For now, we'll use the original text as the target paragraph
    contextContent += correction.originalText
    if (correction.nextParagraph) {
      contextContent += '\n\n...\n\n' + correction.nextParagraph
    }

    setChapterContent(contextContent)
  }

  const generateDiffHtml = (paragraphHtml: string, original: string, suggested: string) => {
    if (!paragraphHtml) return "";

    // Create the highlight HTML
    // We use <span> to avoid breaking block structures like <p>
    const highlightHtml = `
      <span class="bg-red-200 dark:bg-red-900/50 text-red-800 dark:text-red-200 decoration-red-500 line-through decoration-2 mx-1 px-1 rounded">
        ${original}
      </span>
      <span class="bg-green-200 dark:bg-green-900/50 text-green-800 dark:text-green-200 font-bold mx-1 px-1 rounded">
        ${suggested}
      </span>
    `;

    // CAUTION: This replaces the FIRST occurrence of the text in the paragraph.
    // Since Jsoup on backend already isolated the paragraph, this is usually safe.
    // We use replace() instead of replaceAll() to target the likely specific instance.
    return paragraphHtml.replace(original, highlightHtml);
  };

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
                        {correction.novel?.title || `Novel #${correction.novelId.slice(0, 8)}...`}
                      </TableCell>
                      <TableCell>
                        {correction.chapter?.title ? `${correction.chapter.title} (${correction.chapterNumber})` : `Chapter ${correction.chapterNumber}`}
                      </TableCell>
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
              Review the proposed text correction in the context of the affected paragraph
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
                <h4 className="font-semibold mb-2 flex items-center gap-2">
                  Correction in Context
                  <span className="text-xs font-normal text-muted-foreground bg-gray-100 dark:bg-gray-800 px-2 py-0.5 rounded-full">
                    Paragraph #{selectedCorrection.paragraphIndex ?? 'N/A'}
                  </span>
                </h4>

                <div className="p-4 bg-white dark:bg-gray-950 border border-gray-200 dark:border-gray-800 rounded-md shadow-sm max-h-[500px] overflow-y-auto">
                  <div className="font-serif text-lg leading-loose text-gray-700 dark:text-gray-300">

                    {/* 1. PREVIOUS PARAGRAPH (Faded for context) */}
                    {selectedCorrection.previousParagraph && (
                      <div
                        className="opacity-40 hover:opacity-100 transition-opacity duration-200 mb-4 border-l-2 border-transparent pl-2"
                        dangerouslySetInnerHTML={{ __html: selectedCorrection.previousParagraph }}
                      />
                    )}

                    {/* 2. TARGET PARAGRAPH (Highlighted) */}
                    <div className="relative my-4 pl-4 border-l-4 border-yellow-400 bg-yellow-50/50 dark:bg-yellow-900/10 py-2 rounded-r-md">
                      {/* Label */}
                      <span className="absolute -left-1 top-0 -translate-x-full text-xs text-yellow-500 font-sans font-bold pr-2 pt-1">
                        EDIT
                      </span>

                      <div
                        className="text-gray-900 dark:text-gray-100 leading-relaxed whitespace-pre-wrap"
                        dangerouslySetInnerHTML={{
                          __html: (() => {
                            const baseParagraphText = selectedCorrection.paragraphText || selectedCorrection.originalText;
                            const original = selectedCorrection.originalText;
                            const suggested = selectedCorrection.suggestedText;

                            if (!original) return `<p>${baseParagraphText}</p>`;

                            // FIX: Written as a SINGLE LINE string to avoid injecting hidden newlines
                            const deletedHtml = `<span class="bg-red-100 dark:bg-red-950/50 text-red-800 dark:text-red-200 decoration-red-500/70 line-through decoration-2 mx-0.5 px-1 rounded-sm inline">${original}</span>`;

                            // FIX: Written as a SINGLE LINE string
                            const addedHtml = `<span class="bg-green-100 dark:bg-green-950/50 text-green-800 dark:text-green-200 font-semibold mx-0.5 px-1 rounded-sm inline">${suggested}</span>`;

                            // Combine them tightly
                            const replacementHtml = `${deletedHtml}${addedHtml}`;

                            // Use replace() to swap the text
                            return `<p>${baseParagraphText.replace(original, replacementHtml)}</p>`;
                          })()
                        }}
                      />
                    </div>
                    {/* 3. NEXT PARAGRAPH (Faded for context) */}
                    {selectedCorrection.nextParagraph && (
                      <div
                        className="opacity-40 hover:opacity-100 transition-opacity duration-200 mt-4 border-l-2 border-transparent pl-2"
                        dangerouslySetInnerHTML={{ __html: selectedCorrection.nextParagraph }}
                      />
                    )}

                  </div>
                </div>
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
                  <p><strong>Novel:</strong> {selectedCorrection.novel?.title || selectedCorrection.novelId}</p>
                  <p><strong>Chapter:</strong> {selectedCorrection.chapter?.title ? `${selectedCorrection.chapter.title} (${selectedCorrection.chapterNumber})` : `Chapter ${selectedCorrection.chapterNumber}`}</p>
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