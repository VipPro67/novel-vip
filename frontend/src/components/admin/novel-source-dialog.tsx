"use client"

import { useState, useEffect } from "react"
import { api } from "@/services/api"
import { NovelSource, CreateNovelSourceDTO } from "@/services/api/novel-sources"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { toast } from "@/hooks/use-toast"
import { Loader2 } from "lucide-react"
import { useTranslations } from "next-intl"

interface NovelSourceDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  source?: NovelSource | null
  novelId?: string
  onSuccess: () => void
}

export function NovelSourceDialog({
  open,
  onOpenChange,
  source,
  novelId: propNovelId,
  onSuccess,
}: NovelSourceDialogProps) {
  const t = useTranslations("NovelSources.dialog")
  const [loading, setLoading] = useState(false)
  const [novels, setNovels] = useState<Array<{ id: string; title: string }>>([])
  const [formData, setFormData] = useState<CreateNovelSourceDTO>({
    novelId: propNovelId || "",
    sourceUrl: "",
    sourceId: "",
    sourcePlatform: "SHUBA69",
    syncIntervalMinutes: 60,
  })

  // Load novels for selection if no novelId provided
  useEffect(() => {
    if (!propNovelId && open) {
      loadNovels()
    }
  }, [propNovelId, open])

  // Reset form when dialog opens
  useEffect(() => {
    if (open) {
      if (source) {
        setFormData({
          novelId: source.novelId,
          sourceUrl: source.sourceUrl,
          sourceId: source.sourceId,
          sourcePlatform: source.sourcePlatform,
          syncIntervalMinutes: source.syncIntervalMinutes,
        })
      } else {
        setFormData({
          novelId: propNovelId || "",
          sourceUrl: "",
          sourceId: "",
          sourcePlatform: "SHUBA69",
          syncIntervalMinutes: 60,
        })
      }
    }
  }, [open, source, propNovelId])

  const loadNovels = async () => {
    try {
      const response = await api.getNovels({ page: 0, size: 1000 })
      setNovels(
        response.data?.content?.map((n: any) => ({
          id: n.id,
          title: n.title,
        })) || []
      )
    } catch (error) {
      console.error("Failed to load novels:", error)
    }
  }

  const extractSourceId = (url: string): string => {
    // Extract ID from 69shuba URL patterns
    // Example: https://www.69shuba.com/book/12345.html -> 12345
    const match = url.match(/\/book\/(\d+)/)
    return match ? match[1] : ""
  }

  const handleUrlChange = (url: string) => {
    setFormData((prev) => ({
      ...prev,
      sourceUrl: url,
      sourceId: extractSourceId(url),
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!formData.novelId || !formData.sourceUrl) {
      toast({
        title: t("errors.validation"),
        description: t("errors.requiredFields"),
        variant: "destructive",
      })
      return
    }

    try {
      setLoading(true)

      if (source) {
        // Update existing source
        await api.updateNovelSource(source.id, {
          enabled: source.enabled,
          syncIntervalMinutes: formData.syncIntervalMinutes,
          sourceId: formData.sourceId,
        })
        toast({
          title: t("success.updated"),
          description: t("success.updatedDescription"),
        })
      } else {
        // Create new source with quick import
        await api.quickImport(formData)
        toast({
          title: t("success.created"),
          description: t("success.createdDescription"),
        })
      }

      onSuccess()
      onOpenChange(false)
    } catch (error: any) {
      toast({
        title: t("errors.failed"),
        description: error.message || t("errors.failedDescription"),
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[550px]">
        <DialogHeader>
          <DialogTitle>
            {source ? t("titleEdit") : t("titleCreate")}
          </DialogTitle>
          <DialogDescription>
            {source ? t("descriptionEdit") : t("descriptionCreate")}
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Novel Selection (only for create, not edit) */}
          {!source && !propNovelId && (
            <div className="space-y-2">
              <Label htmlFor="novel">{t("fields.novel")}</Label>
              <Select
                value={formData.novelId}
                onValueChange={(value) =>
                  setFormData({ ...formData, novelId: value })
                }
              >
                <SelectTrigger id="novel">
                  <SelectValue placeholder={t("fields.selectNovel")} />
                </SelectTrigger>
                <SelectContent>
                  {novels.map((novel) => (
                    <SelectItem key={novel.id} value={novel.id}>
                      {novel.title}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}

          {/* Source Platform */}
          <div className="space-y-2">
            <Label htmlFor="platform">{t("fields.platform")}</Label>
            <Select
              value={formData.sourcePlatform}
              onValueChange={(value) =>
                setFormData({ ...formData, sourcePlatform: value })
              }
              disabled={!!source}
            >
              <SelectTrigger id="platform">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="SHUBA69">69Shuba</SelectItem>
                <SelectItem value="OTHER">{t("fields.other")}</SelectItem>
              </SelectContent>
            </Select>
            <p className="text-xs text-muted-foreground">
              {t("fields.platformHelp")}
            </p>
          </div>

          {/* Source URL */}
          <div className="space-y-2">
            <Label htmlFor="url">{t("fields.url")}</Label>
            <Input
              id="url"
              placeholder="https://www.69shuba.com/book/12345.html"
              value={formData.sourceUrl}
              onChange={(e) => handleUrlChange(e.target.value)}
              disabled={!!source}
              required
            />
            <p className="text-xs text-muted-foreground">
              {t("fields.urlHelp")}
            </p>
          </div>

          {/* Source ID (auto-extracted) */}
          {formData.sourceId && (
            <div className="space-y-2">
              <Label htmlFor="sourceId">{t("fields.sourceId")}</Label>
              <Input
                id="sourceId"
                value={formData.sourceId}
                onChange={(e) =>
                  setFormData({ ...formData, sourceId: e.target.value })
                }
                disabled={!!source}
              />
              <p className="text-xs text-muted-foreground">
                {t("fields.sourceIdHelp")}
              </p>
            </div>
          )}

          {/* Sync Interval */}
          <div className="space-y-2">
            <Label htmlFor="interval">{t("fields.interval")}</Label>
            <div className="flex gap-2">
              <Input
                id="interval"
                type="number"
                min="30"
                max="1440"
                value={formData.syncIntervalMinutes}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    syncIntervalMinutes: parseInt(e.target.value) || 60,
                  })
                }
                className="flex-1"
                required
              />
              <span className="flex items-center text-sm text-muted-foreground">
                {t("fields.minutes")}
              </span>
            </div>
            <p className="text-xs text-muted-foreground">
              {t("fields.intervalHelp")}
            </p>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={loading}
            >
              {t("actions.cancel")}
            </Button>
            <Button type="submit" disabled={loading}>
              {loading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
              {source ? t("actions.update") : t("actions.create")}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
