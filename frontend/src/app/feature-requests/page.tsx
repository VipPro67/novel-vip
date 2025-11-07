"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { api, type FeatureRequest } from "@/lib/api"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Header } from "@/components/layout/header"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { ThumbsUp, Plus, Loader2, TrendingUp, Clock, CheckCircle2 } from "lucide-react"
import { useToast } from "@/hooks/use-toast"

const STATUS_COLORS = {
  PENDING: "bg-gray-500",
  UNDER_REVIEW: "bg-blue-500",
  PLANNED: "bg-purple-500",
  IN_PROGRESS: "bg-yellow-500",
  COMPLETED: "bg-green-500",
  REJECTED: "bg-red-500",
}

const STATUS_LABELS = {
  PENDING: "Pending",
  UNDER_REVIEW: "Under Review",
  PLANNED: "Planned",
  IN_PROGRESS: "In Progress",
  COMPLETED: "Completed",
  REJECTED: "Rejected",
}

const CATEGORIES = ["UI/UX Improvement", "New Feature", "Performance", "Mobile App", "Content", "Other"]

export default function FeatureRequestsPage() {
  const [featureRequests, setFeatureRequests] = useState<FeatureRequest[]>([])
  const [loading, setLoading] = useState(true)
  const [sortBy, setSortBy] = useState("createdAt")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const { toast } = useToast()

  const [formData, setFormData] = useState({
    title: "",
    description: "",
    category: CATEGORIES[0],
  })

  useEffect(() => {
    fetchFeatureRequests()
  }, [sortBy, statusFilter])

  const fetchFeatureRequests = async () => {
    try {
      setLoading(true)
      const params: any = {
        sortBy,
        sortDir: sortBy === "voteCount" ? "desc" : "desc",
      }

      if (statusFilter !== "all") {
        params.status = statusFilter
      }

      const response = await api.getFeatureRequests(params)
      if (response.success) {
        setFeatureRequests(response.data.content)
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to load feature requests",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleVote = async (featureRequestId: string, hasVoted: boolean) => {
    try {
      if (hasVoted) {
        await api.unvoteFeatureRequest(featureRequestId)
      } else {
        await api.voteFeatureRequest(featureRequestId)
      }

      // Update local state
      setFeatureRequests((prev) =>
        prev.map((fr) =>
          fr.id === featureRequestId
            ? {
                ...fr,
                hasVoted: !hasVoted,
                voteCount: hasVoted ? fr.voteCount - 1 : fr.voteCount + 1,
              }
            : fr,
        ),
      )

      toast({
        title: hasVoted ? "Vote removed" : "Vote added",
        description: hasVoted ? "Your vote has been removed" : "Thanks for your vote!",
      })
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to vote. Please try again.",
        variant: "destructive",
      })
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.title.trim() || !formData.description.trim()) {
      toast({
        title: "Error",
        description: "Please fill in all required fields",
        variant: "destructive",
      })
      return
    }

    try {
      setSubmitting(true)
      const response = await api.createFeatureRequest(formData)

      if (response.success) {
        toast({
          title: "Success",
          description: "Your feature request has been submitted!",
        })
        setIsDialogOpen(false)
        setFormData({
          title: "",
          description: "",
          category: CATEGORIES[0],
        })
        fetchFeatureRequests()
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to submit feature request. Please try again.",
        variant: "destructive",
      })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-background">
      <main className="container py-8">
        <div className="max-w-6xl mx-auto space-y-8">
          <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
            <div>
              <h1 className="text-3xl font-bold">Feature Requests</h1>
              <p className="text-muted-foreground mt-1">Share your ideas and vote on features you'd like to see</p>
            </div>
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  New Request
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[525px]">
                <form onSubmit={handleSubmit}>
                  <DialogHeader>
                    <DialogTitle>Submit Feature Request</DialogTitle>
                    <DialogDescription>Share your idea for improving Novel VIP Pro</DialogDescription>
                  </DialogHeader>
                  <div className="grid gap-4 py-4">
                    <div className="grid gap-2">
                      <Label htmlFor="title">Title *</Label>
                      <Input
                        id="title"
                        placeholder="Brief description of your feature"
                        value={formData.title}
                        onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                        required
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="category">Category</Label>
                      <Select
                        value={formData.category}
                        onValueChange={(value) => setFormData({ ...formData, category: value })}
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          {CATEGORIES.map((cat) => (
                            <SelectItem key={cat} value={cat}>
                              {cat}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="description">Description *</Label>
                      <Textarea
                        id="description"
                        placeholder="Explain your feature request in detail..."
                        value={formData.description}
                        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                        rows={5}
                        required
                      />
                    </div>
                  </div>
                  <DialogFooter>
                    <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                      Cancel
                    </Button>
                    <Button type="submit" disabled={submitting}>
                      {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                      Submit Request
                    </Button>
                  </DialogFooter>
                </form>
              </DialogContent>
            </Dialog>
          </div>

          {/* Filters */}
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <Label className="text-sm mb-2 block">Sort by</Label>
              <Select value={sortBy} onValueChange={setSortBy}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="voteCount">
                    <div className="flex items-center gap-2">
                      <TrendingUp className="h-4 w-4" />
                      Most Voted
                    </div>
                  </SelectItem>
                  <SelectItem value="createdAt">
                    <div className="flex items-center gap-2">
                      <Clock className="h-4 w-4" />
                      Most Recent
                    </div>
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex-1">
              <Label className="text-sm mb-2 block">Status</Label>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Status</SelectItem>
                  <SelectItem value="PENDING">Pending</SelectItem>
                  <SelectItem value="UNDER_REVIEW">Under Review</SelectItem>
                  <SelectItem value="PLANNED">Planned</SelectItem>
                  <SelectItem value="IN_PROGRESS">In Progress</SelectItem>
                  <SelectItem value="COMPLETED">Completed</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Feature Requests List */}
          {loading ? (
            <div className="flex justify-center items-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : featureRequests.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <CheckCircle2 className="h-12 w-12 text-muted-foreground mb-4" />
                <p className="text-muted-foreground text-center">
                  No feature requests found. Be the first to submit one!
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-4">
              {featureRequests.map((request) => (
                <Card key={request.id} className="hover:shadow-md transition-shadow">
                  <CardHeader>
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <Badge className={STATUS_COLORS[request.status]}>{STATUS_LABELS[request.status]}</Badge>
                          <Badge variant="outline">{request.category}</Badge>
                        </div>
                        <CardTitle className="text-xl mb-2">{request.title}</CardTitle>
                        <CardDescription className="text-sm">
                          by {request.createdByUsername} • {new Date(request.createdAt).toLocaleDateString()}
                        </CardDescription>
                      </div>
                      <Button
                        variant={request.hasVoted ? "default" : "outline"}
                        size="sm"
                        onClick={() => handleVote(request.id, request.hasVoted)}
                        className="flex items-center gap-2 min-w-[80px]"
                      >
                        <ThumbsUp className="h-4 w-4" />
                        {request.voteCount}
                      </Button>
                    </div>
                  </CardHeader>
                  <CardContent>
                    <p className="text-muted-foreground whitespace-pre-wrap">{request.description}</p>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  )
}
