"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { api, type FeatureRequest } from "@/services/api"
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
import { useTranslations } from "next-intl"

const STATUS_COLORS = {
  PENDING: "bg-gray-500",
  UNDER_REVIEW: "bg-blue-500",
  PLANNED: "bg-purple-500",
  IN_PROGRESS: "bg-yellow-500",
  COMPLETED: "bg-green-500",
  REJECTED: "bg-red-500",
}

// status label translations are created inside component using the translation hook

const CATEGORIES = ["UI/UX Improvement", "New Feature", "Performance", "Mobile App", "Content", "Other"]

export default function FeatureRequestsPage() {
  const t = useTranslations("FeatureRequests")
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

  const STATUS_LABELS = {
    PENDING: t("filters.pending"),
    UNDER_REVIEW: t("filters.underReview"),
    PLANNED: t("filters.planned"),
    IN_PROGRESS: t("filters.inProgress"),
    COMPLETED: t("filters.completed"),
    REJECTED: t("filters.rejected"),
  }

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
        title: t("toast.errorLoadTitle"),
        description: t("toast.errorLoadDesc"),
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
        title: hasVoted ? t("toast.voteRemovedTitle") : t("toast.voteAddedTitle"),
        description: hasVoted ? t("toast.voteRemovedDesc") : t("toast.voteAddedDesc"),
      })
    } catch (error) {
      toast({
        title: t("toast.errorLoadTitle"),
        description: t("toast.errorVoteDesc"),
        variant: "destructive",
      })
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.title.trim() || !formData.description.trim()) {
      toast({
        title: t("toast.validationErrorTitle"),
        description: t("toast.validationErrorDesc"),
        variant: "destructive",
      })
      return
    }

    try {
      setSubmitting(true)
      const response = await api.createFeatureRequest(formData)

      if (response.success) {
        toast({
          title: t("toast.successSubmitTitle"),
          description: t("toast.successSubmitDesc"),
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
        title: t("toast.errorLoadTitle"),
        description: t("toast.errorSubmitDesc"),
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
              <h1 className="text-3xl font-bold">{t("title")}</h1>
              <p className="text-muted-foreground mt-1">{t("subtitle")}</p>
            </div>
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button>
                  <Plus className="mr-2 h-4 w-4" />
                  {t("newRequest")}
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[525px]">
                <form onSubmit={handleSubmit}>
                  <DialogHeader>
                    <DialogTitle>{t("submitDialogTitle")}</DialogTitle>
                    <DialogDescription>{t("submitDialogDescription")}</DialogDescription>
                  </DialogHeader>
                  <div className="grid gap-4 py-4">
                    <div className="grid gap-2">
                      <Label htmlFor="title">{t("labels.title")}</Label>
                      <Input
                        id="title"
                        placeholder={t("placeholders.title")}
                        value={formData.title}
                        onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                        required
                      />
                    </div>
                    <div className="grid gap-2">
                      <Label htmlFor="category">{t("labels.category")}</Label>
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
                      <Label htmlFor="description">{t("labels.description")}</Label>
                      <Textarea
                        id="description"
                        placeholder={t("placeholders.description")}
                        value={formData.description}
                        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                        rows={5}
                        required
                      />
                    </div>
                  </div>
                  <DialogFooter>
                    <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                      {t("buttons.cancel")}
                    </Button>
                    <Button type="submit" disabled={submitting}>
                      {submitting && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                      {t("buttons.submit")}
                    </Button>
                  </DialogFooter>
                </form>
              </DialogContent>
            </Dialog>
          </div>

          {/* Filters */}
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <Label className="text-sm mb-2 block">{t("filters.sortBy")}</Label>
              <Select value={sortBy} onValueChange={setSortBy}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="voteCount">
                    <div className="flex items-center gap-2">
                      <TrendingUp className="h-4 w-4" />
                      {t("filters.mostVoted")}
                    </div>
                  </SelectItem>
                  <SelectItem value="createdAt">
                    <div className="flex items-center gap-2">
                      <Clock className="h-4 w-4" />
                      {t("filters.mostRecent")}
                    </div>
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex-1">
              <Label className="text-sm mb-2 block">{t("filters.status")}</Label>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">{t("filters.allStatus")}</SelectItem>
                  <SelectItem value="PENDING">{t("filters.pending")}</SelectItem>
                  <SelectItem value="UNDER_REVIEW">{t("filters.underReview")}</SelectItem>
                  <SelectItem value="PLANNED">{t("filters.planned")}</SelectItem>
                  <SelectItem value="IN_PROGRESS">{t("filters.inProgress")}</SelectItem>
                  <SelectItem value="COMPLETED">{t("filters.completed")}</SelectItem>
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
                <p className="text-muted-foreground text-center">{t("emptyList")}</p>
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
                          {t("by")} {request.createdByUsername} • {new Date(request.createdAt).toLocaleDateString()}
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
