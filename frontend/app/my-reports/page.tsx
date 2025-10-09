"use client"

import { useEffect, useState } from "react"
import { api, type Report } from "@/lib/api"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { AlertCircle, CheckCircle2, Clock, XCircle, FileText, MessageSquare, BookOpen, Star } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { Skeleton } from "@/components/ui/skeleton"

const statusConfig = {
  PENDING: {
    label: "Pending",
    icon: Clock,
    color: "bg-yellow-500/10 text-yellow-500 border-yellow-500/20",
  },
  REVIEWED: {
    label: "Reviewed",
    icon: AlertCircle,
    color: "bg-blue-500/10 text-blue-500 border-blue-500/20",
  },
  RESOLVED: {
    label: "Resolved",
    icon: CheckCircle2,
    color: "bg-green-500/10 text-green-500 border-green-500/20",
  },
  DISMISSED: {
    label: "Dismissed",
    icon: XCircle,
    color: "bg-gray-500/10 text-gray-500 border-gray-500/20",
  },
}

const reportTypeConfig = {
  NOVEL: { label: "Novel", icon: BookOpen },
  CHAPTER: { label: "Chapter", icon: FileText },
  COMMENT: { label: "Comment", icon: MessageSquare },
  REVIEW: { label: "Review", icon: Star },
  USER: { label: "User", icon: AlertCircle },
}

const reasonLabels = {
  SPAM: "Spam",
  INAPPROPRIATE_CONTENT: "Inappropriate Content",
  COPYRIGHT_VIOLATION: "Copyright Violation",
  HARASSMENT: "Harassment",
  MISINFORMATION: "Misinformation",
  OTHER: "Other",
}

export default function MyReportsPage() {
  const [reports, setReports] = useState<Report[]>([])
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [statusFilter, setStatusFilter] = useState<string>("ALL")
  const [typeFilter, setTypeFilter] = useState<string>("ALL")
  const { toast } = useToast()

  useEffect(() => {
    fetchReports()
  }, [page, statusFilter, typeFilter])

  const fetchReports = async () => {
    try {
      setLoading(true)
      const params: any = {
        page,
        size: 10,
        sortBy: "createdAt",
        sortDir: "desc",
      }

      if (statusFilter !== "ALL") {
        params.status = statusFilter
      }

      if (typeFilter !== "ALL") {
        params.reportType = typeFilter
      }

      const response = await api.getMyReports(params)

      if (response.success) {
        setReports(response.data.content)
        setTotalPages(response.data.totalPages)
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load reports. Please try again.",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    })
  }

  return (
    <main className="container mx-auto px-4 py-8 max-w-6xl">
      <div className="mb-8">
        <h1 className="text-4xl font-bold mb-2">My Reports</h1>
        <p className="text-muted-foreground">Track the status of your submitted reports</p>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-4 mb-6">
        <div className="flex-1">
          <label className="text-sm font-medium mb-2 block">Filter by Status</label>
          <Tabs value={statusFilter} onValueChange={setStatusFilter}>
            <TabsList className="grid w-full grid-cols-5">
              <TabsTrigger value="ALL">All</TabsTrigger>
              <TabsTrigger value="PENDING">Pending</TabsTrigger>
              <TabsTrigger value="REVIEWED">Reviewed</TabsTrigger>
              <TabsTrigger value="RESOLVED">Resolved</TabsTrigger>
              <TabsTrigger value="DISMISSED">Dismissed</TabsTrigger>
            </TabsList>
          </Tabs>
        </div>

        <div className="w-full sm:w-48">
          <label className="text-sm font-medium mb-2 block">Report Type</label>
          <Select value={typeFilter} onValueChange={setTypeFilter}>
            <SelectTrigger>
              <SelectValue placeholder="All Types" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">All Types</SelectItem>
              <SelectItem value="NOVEL">Novel</SelectItem>
              <SelectItem value="CHAPTER">Chapter</SelectItem>
              <SelectItem value="COMMENT">Comment</SelectItem>
              <SelectItem value="REVIEW">Review</SelectItem>
              <SelectItem value="USER">User</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Reports List */}
      {loading ? (
        <div className="space-y-4">
          {[...Array(3)].map((_, i) => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-6 w-3/4" />
                <Skeleton className="h-4 w-1/2" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-20 w-full" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : reports.length === 0 ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <AlertCircle className="h-12 w-12 text-muted-foreground mb-4" />
            <h3 className="text-lg font-semibold mb-2">No reports found</h3>
            <p className="text-muted-foreground text-center">You haven't submitted any reports yet.</p>
          </CardContent>
        </Card>
      ) : (
        <div className="space-y-4">
          {reports.map((report) => {
            const StatusIcon = statusConfig[report.status].icon
            const TypeIcon = reportTypeConfig[report.reportType].icon

            return (
              <Card key={report.id}>
                <CardHeader>
                  <div className="flex items-start justify-between gap-4">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <TypeIcon className="h-4 w-4 text-muted-foreground" />
                        <CardTitle className="text-lg">{reportTypeConfig[report.reportType].label} Report</CardTitle>
                      </div>
                      <CardDescription className="flex items-center gap-2">
                        <span>Reported on {formatDate(report.createdAt)}</span>
                        <span>•</span>
                        <span>ID: {report.targetId.slice(0, 8)}...</span>
                      </CardDescription>
                    </div>
                    <Badge variant="outline" className={`flex items-center gap-1 ${statusConfig[report.status].color}`}>
                      <StatusIcon className="h-3 w-3" />
                      {statusConfig[report.status].label}
                    </Badge>
                  </div>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div>
                    <span className="text-sm font-medium">Reason: </span>
                    <Badge variant="secondary">{reasonLabels[report.reason]}</Badge>
                  </div>
                  {report.description && (
                    <div>
                      <span className="text-sm font-medium block mb-1">Description:</span>
                      <p className="text-sm text-muted-foreground bg-muted p-3 rounded-md">{report.description}</p>
                    </div>
                  )}
                  {report.updatedAt !== report.createdAt && (
                    <div className="text-xs text-muted-foreground">Last updated: {formatDate(report.updatedAt)}</div>
                  )}
                </CardContent>
              </Card>
            )
          })}
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 mt-8">
          <Button variant="outline" onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0 || loading}>
            Previous
          </Button>
          <span className="text-sm text-muted-foreground">
            Page {page + 1} of {totalPages}
          </span>
          <Button
            variant="outline"
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1 || loading}
          >
            Next
          </Button>
        </div>
      )}
    </main>
  )
}
