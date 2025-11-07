"use client"

import { useState, useEffect } from "react"
import { Clock, Check, X, Shield } from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { useToast } from "@/hooks/use-toast"
import { api} from "@/services/api"
import { Pagination } from "@/components/ui/pagination"
import { usePagination } from "@/hooks/use-pagination"
import { ERole, RoleRequest } from "@/models"

export function MyRoleRequests() {
  const [requests, setRequests] = useState<RoleRequest[]>([])
  const [loading, setLoading] = useState(true)
  const { toast } = useToast()

  const { currentPage, totalPages, handlePageChange, updateTotalPages, getPaginationParams } = usePagination({
    initialPage: 0,
    initialSize: 10,
    initialSortBy: "createdAt",
    initialSortDir: "desc",
  })

  useEffect(() => {
    fetchMyRequests()
  }, [currentPage, getPaginationParams])

  const fetchMyRequests = async () => {
    setLoading(true)
    try {
      const paginationParams = getPaginationParams()
      const response = await api.getMyRoleRequests(paginationParams)

      if (response.success && response.data?.content) {
        setRequests(response.data.content || [])
        updateTotalPages(response.data.totalPages || 0)
      }
    } catch (error) {
      console.error("Failed to fetch role requests:", error)
      toast({
        title: "Error",
        description: "Failed to fetch your role requests",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "PENDING":
        return <Clock className="h-4 w-4 text-yellow-500" />
      case "APPROVED":
        return <Check className="h-4 w-4 text-green-500" />
      case "REJECTED":
        return <X className="h-4 w-4 text-red-500" />
      default:
        return <Clock className="h-4 w-4 text-gray-500" />
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "PENDING":
        return "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200"
      case "APPROVED":
        return "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200"
      case "REJECTED":
        return "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200"
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200"
    }
  }

  const getRoleBadgeColor = (erole: ERole) => {
    switch (erole) {
      case ERole.ADMIN:
        return "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200"
      case ERole.MODERATOR:
        return "bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200"
      case ERole.AUTHOR:
        return "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200"
      default:
        return "bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200"
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center space-x-2">
          <Shield className="h-5 w-5" />
          <span>My Role Requests</span>
        </CardTitle>
        <CardDescription>View the status of your role requests</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {/* Requests Table */}
          <div className="border rounded-md">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Requested Role</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Requested Date</TableHead>
                  <TableHead>Reviewed Date</TableHead>
                  <TableHead>Reviewer</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  Array.from({ length: 3 }).map((_, i) => (
                    <TableRow key={i}>
                      <TableCell>
                        <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-16 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-24 bg-muted rounded animate-pulse" />
                      </TableCell>                    
                      <TableCell>
                        <div className="h-4 w-24 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                      </TableCell>
                    </TableRow>
                  ))
                ) : !requests || requests.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center py-8">
                      <div className="flex flex-col items-center space-y-2">
                        <Shield className="h-8 w-8 text-muted-foreground" />
                        <p className="text-muted-foreground">No role requests found</p>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  requests.map((request) => (
                    <TableRow key={request.id}>
                      <TableCell>
                        <Badge className={getRoleBadgeColor(request.requestedRole)}>{request.requestedRole}</Badge>
                      </TableCell>
                      <TableCell>
                        <Badge className={getStatusColor(request.status)} variant="outline">
                          <div className="flex items-center space-x-1">
                            {getStatusIcon(request.status)}
                            <span>{request.status}</span>
                          </div>
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <span className="text-sm text-muted-foreground">
                          {new Date(request.createdAt).toLocaleDateString()}
                        </span>
                      </TableCell>
                      <TableCell>
                        {request.createdAt ? (
                          <span className="text-sm text-muted-foreground">
                            {new Date(request.createdAt).toLocaleDateString()}
                          </span>
                        ) : (
                          <span className="text-sm text-muted-foreground">-</span>
                        )}
                      </TableCell>
                      <TableCell>
                        {request.processedBy ? (
                          <span className="text-sm text-muted-foreground">{request.processedBy}</span>
                        ) : (
                          <span className="text-sm text-muted-foreground">-</span>
                        )}
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
            onPageChange={handlePageChange}
            showPageNumbers={true}
            className="mt-4"
          />
        </div>
      </CardContent>
    </Card>
  )
}
