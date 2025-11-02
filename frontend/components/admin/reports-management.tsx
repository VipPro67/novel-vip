"use client"

import { useState, useEffect } from "react"
import { CheckCircle, AlertCircle, Eye } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
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
import { api, type Report } from "@/lib/api"
import { Pagination } from "@/components/ui/pagination"
import { usePagination } from "@/hooks/use-pagination"

export function ReportsManagement() {
  const [reports, setReports] = useState<Report[]>([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState<string>("")
  const { currentPage, totalPages, handlePageChange, updateTotalPages, resetPage, getPaginationParams } = usePagination(
    {
      initialPage: 0,
      initialSize: 10,
      initialSortBy: "createdAt",
      initialSortDir: "desc",
    },
  )
  const [selectedReport, setSelectedReport] = useState<Report | null>(null)
  const [viewDialog, setViewDialog] = useState(false)
  const [resolveDialog, setResolveDialog] = useState(false)
  const [resolveAction, setResolveAction] = useState("")
  const { toast } = useToast()

  const statuses = ["PENDING", "REVIEWED", "RESOLVED", "DISMISSED"]
  const statusColors: Record<string, string> = {
    PENDING: "default",
    REVIEWED: "secondary",
    RESOLVED: "default",
    DISMISSED: "outline",
  }

  useEffect(() => {
    fetchReports()
  }, [currentPage, statusFilter, getPaginationParams, resetPage])

  const fetchReports = async () => {
    setLoading(true)
    try {
      const paginationParams = getPaginationParams()
      const response = await api.getAllReports({
        ...paginationParams,
        status: statusFilter || undefined,
      })
      if (response.success) {
        setReports(response.data.content)
        updateTotalPages(response.data.totalPages)
      }
    } catch (error) {
      console.error("Failed to fetch reports:", error)
      toast({
        title: "Error",
        description: "Failed to fetch reports",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleResolve = async () => {
    if (!selectedReport || !resolveAction.trim()) return
    try {
      await api.resolveReport(selectedReport.id, resolveAction)
      toast({
        title: "Success",
        description: "Report resolved",
      })
      setResolveDialog(false)
      setResolveAction("")
      fetchReports()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to resolve report",
        variant: "destructive",
      })
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Reports Management</CardTitle>
        <CardDescription>Handle content and user reports</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div className="flex items-center space-x-2">
            <Input
              placeholder="Filter by status..."
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value)
                resetPage()
              }}
              className="max-w-sm"
            />
            {statusFilter && (
              <Button variant="outline" size="sm" onClick={() => setStatusFilter("")}>
                Clear
              </Button>
            )}
          </div>

          <div className="border rounded-md">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Type</TableHead>
                  <TableHead>Reason</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Reported By</TableHead>
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
                        <div className="h-4 w-32 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-16 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-8 bg-muted rounded animate-pulse" />
                      </TableCell>
                    </TableRow>
                  ))
                ) : reports.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center py-8">
                      No reports found
                    </TableCell>
                  </TableRow>
                ) : (
                  reports.map((report) => (
                    <TableRow key={report.id}>
                      <TableCell>
                        <Badge variant="secondary">{report.reportType}</Badge>
                      </TableCell>
                      <TableCell className="text-sm">{report.reason}</TableCell>
                      <TableCell>
                        <Badge variant={statusColors[report.status] as any}>{report.status}</Badge>
                      </TableCell>
                      <TableCell className="text-sm">@{report.reportedBy}</TableCell>
                      <TableCell className="text-sm">{new Date(report.createdAt).toLocaleDateString()}</TableCell>
                      <TableCell>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon">
                              <AlertCircle className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem
                              onClick={() => {
                                setSelectedReport(report)
                                setViewDialog(true)
                              }}
                            >
                              <Eye className="mr-2 h-4 w-4" />
                              View Details
                            </DropdownMenuItem>
                            <DropdownMenuItem
                              onClick={() => {
                                setSelectedReport(report)
                                setResolveDialog(true)
                              }}
                            >
                              <CheckCircle className="mr-2 h-4 w-4" />
                              Resolve
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
            <DialogTitle>Report Details</DialogTitle>
          </DialogHeader>
          {selectedReport && (
            <div className="space-y-4">
              <div>
                <p className="text-sm font-medium">Type</p>
                <p className="text-sm text-muted-foreground">{selectedReport.reportType}</p>
              </div>
              <div>
                <p className="text-sm font-medium">Reason</p>
                <p className="text-sm text-muted-foreground">{selectedReport.reason}</p>
              </div>
              <div>
                <p className="text-sm font-medium">Description</p>
                <p className="text-sm text-muted-foreground mt-1">{selectedReport.description}</p>
              </div>
              <div>
                <p className="text-sm font-medium">Status</p>
                <Badge className="mt-1">{selectedReport.status}</Badge>
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

      <Dialog open={resolveDialog} onOpenChange={setResolveDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Resolve Report</DialogTitle>
            <DialogDescription>Select an action to resolve this report</DialogDescription>
          </DialogHeader>
          <div className="space-y-2">
            <Button
              variant={resolveAction === "APPROVED" ? "default" : "outline"}
              className="w-full justify-start"
              onClick={() => setResolveAction("APPROVED")}
            >
              Approve (Report is valid)
            </Button>
            <Button
              variant={resolveAction === "REJECTED" ? "default" : "outline"}
              className="w-full justify-start"
              onClick={() => setResolveAction("REJECTED")}
            >
              Reject (Report is invalid)
            </Button>
            <Button
              variant={resolveAction === "ACTION_TAKEN" ? "default" : "outline"}
              className="w-full justify-start"
              onClick={() => setResolveAction("ACTION_TAKEN")}
            >
              Action Taken
            </Button>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setResolveDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleResolve} disabled={!resolveAction}>
              Resolve
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Card>
  )
}
