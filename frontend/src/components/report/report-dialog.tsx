"use client"

import type React from "react"

import { useState } from "react"
import { Flag, Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useToast } from "@/hooks/use-toast"
import { useAuth } from "@/components/providers/auth-provider"
import { api } from "@/services/api"
import { useRouter } from "next/navigation"

interface ReportDialogProps {
  reportType: "NOVEL" | "CHAPTER" | "COMMENT" | "REVIEW" | "USER"
  targetId: string
  targetTitle?: string
  trigger?: React.ReactNode
}

const NOVEL_REASONS = [
  { value: "MISSING_INFO", label: "Missing Information" },
  { value: "MISSING_CHAPTER", label: "Missing Chapter" },
  { value: "WRONG_INFO", label: "Wrong Information" },
  { value: "COPYRIGHT_VIOLATION", label: "Copyright Issue" },
  { value: "OTHER", label: "Other" },
] as const

const CHAPTER_REASONS = [
  { value: "WRONG_CHAPTER", label: "Wrong Chapter" },
  { value: "ERROR_CONTENT", label: "Error in Content" },
  { value: "MISSING_CONTENT", label: "Missing Content" },
  { value: "POOR_QUALITY", label: "Poor Quality" },
  { value: "OTHER", label: "Other" },
] as const

const COMMENT_REASONS = [
  { value: "SPAM", label: "Spam" },
  { value: "VIOLENCE", label: "Violence/Harassment" },
  { value: "INAPPROPRIATE_CONTENT", label: "Inappropriate Content" },
  { value: "MISINFORMATION", label: "Misinformation" },
  { value: "OTHER", label: "Other" },
] as const

const GENERIC_REASONS = [
  { value: "SPAM", label: "Spam" },
  { value: "INAPPROPRIATE_CONTENT", label: "Inappropriate Content" },
  { value: "COPYRIGHT_VIOLATION", label: "Copyright Violation" },
  { value: "HARASSMENT", label: "Harassment" },
  { value: "MISINFORMATION", label: "Misinformation" },
  { value: "OTHER", label: "Other" },
] as const

export function ReportDialog({ reportType, targetId, targetTitle, trigger }: ReportDialogProps) {
  const [open, setOpen] = useState(false)
  const [reason, setReason] = useState<string>("")
  const [description, setDescription] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const { toast } = useToast()
  const { isAuthenticated } = useAuth()
  const router = useRouter()

  const getReasons = () => {
    switch (reportType) {
      case "NOVEL":
        return NOVEL_REASONS
      case "CHAPTER":
        return CHAPTER_REASONS
      case "COMMENT":
        return COMMENT_REASONS
      default:
        return GENERIC_REASONS
    }
  }

  const handleSubmit = async () => {
    if (!isAuthenticated) {
      router.push("/login")
      return
    }

    if (!reason || !description.trim()) {
      toast({
        title: "Error",
        description: "Please select a reason and provide a description",
        variant: "destructive",
      })
      return
    }

    setSubmitting(true)
    try {
      const response = await api.createReport({
        reportType,
        targetId,
        reason: reason as any,
        description: description.trim(),
      })

      if (response.success) {
        toast({
          title: "Report submitted",
          description: "Thank you for helping us maintain a safe community. We will review your report.",
        })
        setOpen(false)
        setReason("")
        setDescription("")
      }
    } catch (error) {
      console.error("Failed to submit report:", error)
      toast({
        title: "Error",
        description: "Failed to submit report. Please try again.",
        variant: "destructive",
      })
    } finally {
      setSubmitting(false)
    }
  }

  const getReportTypeLabel = () => {
    switch (reportType) {
      case "NOVEL":
        return "novel"
      case "CHAPTER":
        return "chapter"
      case "COMMENT":
        return "comment"
      case "REVIEW":
        return "review"
      case "USER":
        return "user"
      default:
        return "content"
    }
  }

  const reasons = getReasons()

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        {trigger || (
          <Button variant="ghost" size="sm">
            <Flag className="h-4 w-4 mr-2" />
            Report
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Report {getReportTypeLabel()}</DialogTitle>
          <DialogDescription>
            Help us maintain a safe and respectful community by reporting inappropriate content.
            {targetTitle && <span className="block mt-2 font-medium text-foreground">Reporting: {targetTitle}</span>}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="reason">Reason for report</Label>
            <Select value={reason} onValueChange={setReason}>
              <SelectTrigger id="reason">
                <SelectValue placeholder="Select a reason" />
              </SelectTrigger>
              <SelectContent>
                {reasons.map((r) => (
                  <SelectItem key={r.value} value={r.value}>
                    {r.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Additional details</Label>
            <Textarea
              id="description"
              placeholder="Please provide more details about why you're reporting this content..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="min-h-[120px]"
            />
            <p className="text-xs text-muted-foreground">
              Be specific and provide as much detail as possible to help us review your report.
            </p>
          </div>
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)} disabled={submitting}>
            Cancel
          </Button>
          <Button onClick={handleSubmit} disabled={!reason || !description.trim() || submitting}>
            {submitting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                Submitting...
              </>
            ) : (
              <>
                <Flag className="mr-2 h-4 w-4" />
                Submit Report
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
