"use client"

import { useState, useEffect } from "react"
import { Check, X, Clock, Shield, MessageSquare } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { useToast } from "@/hooks/use-toast"
import { api, type RoleRequest } from "@/lib/api"
import { Pagination } from "@/components/ui/pagination"
import { usePagination } from "@/hooks/use-pagination"

export function RoleApprovalManagement() {
  const [requests, setRequests] = useState<RoleRequest[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedRequest, setSelectedRequest] = useState<RoleRequest | null>(null)
  const [approveDialog, setApproveDialog] = useState(false)
  const [rejectDialog, setRejectDialog] = useState(false)
  const [viewDialog, setViewDialog] = useState(false)
  const { toast } = useToast()

  const { currentPage, totalPages, handlePageChange, updateTotalPages, getPaginationParams } = usePagination({
    initialPage: 0,
    initialSize: 10,
    initialSortBy: "createdAt",
    initialSortDir: "desc",
  })

  useEffect(() => {
    fetchPendingRequests()
  }, [currentPage, getPaginationParams])

  const fetchPendingRequests = async () => {
    setLoading(true)
    try {
      const paginationParams = getPaginationParams()
      const response = await api.getPendingRoleRequests(paginationParams)

      console.log("Pending role requests response:", response)

      if (response.success) {
        setRequests(response.data.content)
        updateTotalPages(response.data.totalPages)
      }
    } catch (error) {
      console.error("Failed to fetch pending role requests:", error)
      toast({
        title: "Error",
        description: "Failed to fetch pending role requests",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleViewRequest = (request: RoleRequest) => {
    setSelectedRequest(request)
    setViewDialog(true)
  }

  const handleApproveRequest = (request: RoleRequest) => {
    setSelectedRequest(request)
    setApproveDialog(true)
  }

  const handleRejectRequest = (request: RoleRequest) => {
    setSelectedRequest(request)
    setRejectDialog(true)
  }

  const confirmApprove = async () => {
    if (!selectedRequest) return

    try {
      const response = await api.approveRoleRequest(selectedRequest.id)
      if (response.success) {
        toast({
          title: "Success",
          description: `Role request approved for ${selectedRequest.username}`,
        })
        setApproveDialog(false)
        fetchPendingRequests()
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to approve role request",
        variant: "destructive",
      })
    }
  }

  const confirmReject = async () => {
    if (!selectedRequest) return

    try {
      const response = await api.rejectRoleRequest(selectedRequest.id)
      if (response.success) {
        toast({
          title: "Success",
          description: `Role request rejected for ${selectedRequest.username}`,
        })
        setRejectDialog(false)
        fetchPendingRequests()
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to reject role request",
        variant: "destructive",
      })
    }
  }

  const getRoleBadgeColor = (role: string) => {
    switch (role.toUpperCase()) {
      case "ADMIN":
        return "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200"
      case "MODERATOR":
        return "bg-orange-100 text-orange-800 dark:bg-orange-900 dark:text-orange-200"
      case "AUTHOR":
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
          <span>Role Approval Management</span>
        </CardTitle>
        <CardDescription>Review and approve user role requests</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {/* Pending Requests Table */}
          <div className="border rounded-md">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>User</TableHead>
                  <TableHead>Requested Role</TableHead>
                  <TableHead>Requested Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="w-[200px]">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  Array.from({ length: 5 }).map((_, i) => (
                    <TableRow key={i}>
                      <TableCell>
                        <div className="flex items-center space-x-2">
                          <div className="h-8 w-8 bg-muted rounded-full animate-pulse" />
                          <div className="space-y-1">
                            <div className="h-4 w-24 bg-muted rounded animate-pulse" />
                            <div className="h-3 w-32 bg-muted rounded animate-pulse" />
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-24 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="h-4 w-16 bg-muted rounded animate-pulse" />
                      </TableCell>
                      <TableCell>
                        <div className="flex space-x-2">
                          <div className="h-8 w-16 bg-muted rounded animate-pulse" />
                          <div className="h-8 w-16 bg-muted rounded animate-pulse" />
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                ) : requests.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center py-8">
                      <div className="flex flex-col items-center space-y-2">
                        <Clock className="h-8 w-8 text-muted-foreground" />
                        <p className="text-muted-foreground">No pending role requests</p>
                      </div>
                    </TableCell>
                  </TableRow>
                ) : (
                  requests.map((request) => (
                    <TableRow key={request.id}>
                      <TableCell>
                        <div className="flex items-center space-x-2">
                          <Avatar className="h-8 w-8">
                            <AvatarImage src="/placeholder.svg?height=32&width=32" />
                            <AvatarFallback>{request.username.charAt(0).toUpperCase()}</AvatarFallback>
                          </Avatar>
                          <div>
                            <p className="font-medium">{request.username}</p>
                            <p className="text-sm text-muted-foreground">{request.email}</p>
                          </div>
                        </div>
                      </TableCell>
                      <TableCell>
                        <Badge className={getRoleBadgeColor(request.requestedRole)}>{request.requestedRole}</Badge>
                      </TableCell>
                      <TableCell>
                        <span className="text-sm text-muted-foreground">
                          {new Date(request.createdAt).toLocaleDateString()}
                        </span>
                      </TableCell>
                      <TableCell>
                        <Badge variant="outline" className="flex items-center space-x-1 w-fit">
                          <Clock className="h-3 w-3" />
                          <span>{request.status}</span>
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center space-x-2">
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleViewRequest(request)}
                            className="flex items-center space-x-1"
                          >
                            <MessageSquare className="h-3 w-3" />
                            <span>View</span>
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleApproveRequest(request)}
                            className="flex items-center space-x-1 text-green-600 hover:text-green-700"
                          >
                            <Check className="h-3 w-3" />
                            <span>Approve</span>
                          </Button>
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleRejectRequest(request)}
                            className="flex items-center space-x-1 text-red-600 hover:text-red-700"
                          >
                            <X className="h-3 w-3" />
                            <span>Reject</span>
                          </Button>
                        </div>
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

      {/* View Request Dialog */}
      <Dialog open={viewDialog} onOpenChange={setViewDialog}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Role Request Details</DialogTitle>
            <DialogDescription>Review the role request information</DialogDescription>
          </DialogHeader>
          {selectedRequest && (
            <div className="space-y-4">
              <div className="flex items-center space-x-3">
                <Avatar className="h-12 w-12">
                  <AvatarImage src="/placeholder.svg?height=48&width=48" />
                  <AvatarFallback>{selectedRequest.username.charAt(0).toUpperCase()}</AvatarFallback>
                </Avatar>
                <div>
                  <p className="font-medium">{selectedRequest.username}</p>
                  <p className="text-sm text-muted-foreground">{selectedRequest.email}</p>
                </div>
              </div>

              <div className="space-y-2">
                <Label>Requested Role</Label>
                <Badge className={getRoleBadgeColor(selectedRequest.requestedRole)}>
                  {selectedRequest.requestedRole}
                </Badge>
              </div>

              <div className="space-y-2">
                <Label>Reason</Label>
                <div className="p-3 bg-muted rounded-md">
                  <p className="text-sm">{selectedRequest.reason}</p>
                </div>
              </div>

              <div className="space-y-2">
                <Label>Request Date</Label>
                <p className="text-sm text-muted-foreground">
                  {new Date(selectedRequest.createdAt).toLocaleString()}
                </p>
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

      {/* Approve Request Dialog */}
      <Dialog open={approveDialog} onOpenChange={setApproveDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Approve Role Request</DialogTitle>
            <DialogDescription>
              Are you sure you want to approve the {selectedRequest?.requestedRole} role request for{" "}
              {selectedRequest?.username}?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setApproveDialog(false)}>
              Cancel
            </Button>
            <Button onClick={confirmApprove} className="bg-green-600 hover:bg-green-700">
              <Check className="mr-2 h-4 w-4" />
              Approve Request
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Reject Request Dialog */}
      <Dialog open={rejectDialog} onOpenChange={setRejectDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reject Role Request</DialogTitle>
            <DialogDescription>
              Are you sure you want to reject the {selectedRequest?.requestedRole} role request for{" "}
              {selectedRequest?.username}?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRejectDialog(false)}>
              Cancel
            </Button>
            <Button onClick={confirmReject} variant="destructive">
              <X className="mr-2 h-4 w-4" />
              Reject Request
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </Card>
  )
}
