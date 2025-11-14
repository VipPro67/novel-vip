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
import { useRouter } from "@/navigation"
import { useTranslations } from "next-intl"

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
  const t = useTranslations("Report")
  const [open, setOpen] = useState(false)
  const [reason, setReason] = useState<string>("")
  const [description, setDescription] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const { toast } = useToast()
  const { isAuthenticated } = useAuth()
  const router = useRouter()

  const getReasons = () => {
    const map = (arr: readonly { value: string; label: string }[]) =>
      arr.map((r) => ({ value: r.value, label: t(`reasons.${r.value}`) }))

    switch (reportType) {
      case "NOVEL":
        return map(NOVEL_REASONS)
      case "CHAPTER":
        return map(CHAPTER_REASONS)
      case "COMMENT":
        return map(COMMENT_REASONS)
      default:
        return map(GENERIC_REASONS)
    }
  }

  const handleSubmit = async () => {
    if (!isAuthenticated) {
      router.push("/login")
      return
    }

    if (!reason || !description.trim()) {
      toast({
        title: t("toasts.validationErrorTitle"),
        description: t("toasts.validationErrorDesc"),
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
          title: t("toasts.successTitle"),
          description: t("toasts.successDesc"),
        })
        setOpen(false)
        setReason("")
        setDescription("")
      }
    } catch (error) {
      console.error("Failed to submit report:", error)
      toast({
        title: t("toasts.validationErrorTitle"),
        description: t("toasts.errorSubmitDesc"),
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
            {t("trigger")}
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{t.rich("title", { type: getReportTypeLabel() })}</DialogTitle>
          <DialogDescription>
            {t("description")}
            {targetTitle && (
              <span className="block mt-2 font-medium text-foreground">{t.rich("reporting", { title: targetTitle })}</span>
            )}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="reason">{t("labels.reason")}</Label>
            <Select value={reason} onValueChange={setReason}>
              <SelectTrigger id="reason">
                <SelectValue placeholder={t("placeholders.selectReason")} />
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
            <Label htmlFor="description">{t("labels.description")}</Label>
            <Textarea
              id="description"
              placeholder={t("placeholders.description")}
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
            {t("buttons.cancel")}
          </Button>
          <Button onClick={handleSubmit} disabled={!reason || !description.trim() || submitting}>
            {submitting ? (
              <>
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                {t("buttons.submitting")}
              </>
            ) : (
              <>
                <Flag className="mr-2 h-4 w-4" />
                {t("buttons.submit")}
              </>
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
