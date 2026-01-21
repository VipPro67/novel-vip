"use client"

import { useState, useEffect, useMemo } from "react"
import { api } from "@/services/api"
import { NovelSource, SyncStatus } from "@/services/api/novel-sources"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { toast } from "@/hooks/use-toast"
import { 
  Loader2, 
  Play, 
  Pause, 
  Trash2, 
  Plus, 
  RefreshCw, 
  Search,
  ExternalLink,
  AlertCircle,
  CheckCircle,
  Clock
} from "lucide-react"
import { NovelSourceDialog } from "@/components/admin/novel-source-dialog"
import { useTranslations } from "next-intl"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"

type FilterStatus = 'all' | 'enabled' | 'disabled' | 'failed'

export function NovelSourcesManagement({ novelId }: { novelId?: string }) {
  const t = useTranslations("NovelSources")
  
  // State management
  const [sources, setSources] = useState<NovelSource[]>([])
  const [loading, setLoading] = useState(true)
  const [syncing, setSyncing] = useState<Set<string>>(new Set())
  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingSource, setEditingSource] = useState<NovelSource | null>(null)
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null)
  
  // Filter state
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<FilterStatus>("all")

  useEffect(() => {
    loadSources()
    // Set up polling for active syncs
    const interval = setInterval(() => {
      if (syncing.size > 0) {
        loadSources()
      }
    }, 5000)
    return () => clearInterval(interval)
  }, [novelId, syncing.size])

  const loadSources = async () => {
    try {
      setLoading(true)
      const response = novelId
        ? await api.getNovelSourcesByNovelId(novelId)
        : await api.getAllNovelSources()
      setSources(response.data || [])
      
      // Update syncing state based on actual status
      const activeSyncs = new Set(
        (response.data || [])
          .filter((s: NovelSource) => s.syncStatus === 'SYNCING')
          .map((s: NovelSource) => s.id)
      )
      setSyncing(activeSyncs)
    } catch (error) {
      console.error("Failed to load sources:", error)
      toast({
        title: t("errors.loadFailed"),
        description: t("errors.loadFailedDescription"),
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  // Filtered sources with memoization for performance
  const filteredSources = useMemo(() => {
    return sources.filter((source) => {
      // Search filter
      const matchesSearch = searchQuery === "" || 
        source.novelTitle.toLowerCase().includes(searchQuery.toLowerCase()) ||
        source.sourceUrl.toLowerCase().includes(searchQuery.toLowerCase())
      
      // Status filter
      const matchesStatus = 
        statusFilter === "all" ||
        (statusFilter === "enabled" && source.enabled) ||
        (statusFilter === "disabled" && !source.enabled) ||
        (statusFilter === "failed" && source.syncStatus === "FAILED")
      
      return matchesSearch && matchesStatus
    })
  }, [sources, searchQuery, statusFilter])

  const handleAddSource = () => {
    setEditingSource(null)
    setDialogOpen(true)
  }

  const handleEditSource = (source: NovelSource) => {
    setEditingSource(source)
    setDialogOpen(true)
  }

  const handleToggleEnabled = async (source: NovelSource) => {
    try {
      await api.updateNovelSource(source.id, {
        enabled: !source.enabled,
      })
      toast({
        title: t("success.toggled"),
        description: source.enabled ? t("success.disabled") : t("success.enabled"),
      })
      loadSources()
    } catch (error) {
      toast({
        title: t("errors.updateFailed"),
        description: t("errors.updateFailedDescription"),
        variant: "destructive",
      })
    }
  }

  const handleSync = async (sourceId: string, fullImport = false) => {
    try {
      setSyncing(prev => new Set(prev).add(sourceId))
      await api.triggerSync(sourceId, { fullImport })
      toast({
        title: t("success.syncStarted"),
        description: fullImport ? t("success.fullImportStarted") : t("success.syncStartedDescription"),
      })
      // Reload after a short delay to get updated status
      setTimeout(loadSources, 1000)
    } catch (error: any) {
      toast({
        title: t("errors.syncFailed"),
        description: error.message || t("errors.syncFailedDescription"),
        variant: "destructive",
      })
      setSyncing(prev => {
        const next = new Set(prev)
        next.delete(sourceId)
        return next
      })
    }
  }

  const handleDelete = async (sourceId: string) => {
    try {
      await api.deleteNovelSource(sourceId)
      toast({
        title: t("success.deleted"),
        description: t("success.deletedDescription"),
      })
      loadSources()
    } catch (error) {
      toast({
        title: t("errors.deleteFailed"),
        description: t("errors.deleteFailedDescription"),
        variant: "destructive",
      })
    } finally {
      setDeleteConfirmId(null)
    }
  }

  const getSyncStatusBadge = (status: SyncStatus) => {
    const config = {
      IDLE: {
        variant: "secondary" as const,
        icon: Clock,
        label: t("status.idle"),
        animate: false
      },
      SYNCING: {
        variant: "default" as const,
        icon: Loader2,
        label: t("status.syncing"),
        animate: true
      },
      SUCCESS: {
        variant: "default" as const,
        icon: CheckCircle,
        label: t("status.success"),
        animate: false
      },
      FAILED: {
        variant: "destructive" as const,
        icon: AlertCircle,
        label: t("status.failed"),
        animate: false
      },
    }

    const { variant, icon: Icon, label, animate } = config[status]

    return (
      <Badge variant={variant} className="gap-1">
        <Icon className={`h-3 w-3 ${animate ? 'animate-spin' : ''}`} />
        {label}
      </Badge>
    )
  }

  const formatRelativeTime = (dateString: string) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / 60000)
    
    if (diffMins < 1) return t("time.justNow")
    if (diffMins < 60) return t("time.minutesAgo", { count: diffMins })
    
    const diffHours = Math.floor(diffMins / 60)
    if (diffHours < 24) return t("time.hoursAgo", { count: diffHours })
    
    const diffDays = Math.floor(diffHours / 24)
    if (diffDays < 30) return t("time.daysAgo", { count: diffDays })
    
    return date.toLocaleDateString()
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center p-12">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header and Actions */}
      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold">{t("title")}</h2>
          <p className="text-sm text-muted-foreground">
            {t("totalSources", { count: sources.length })}
          </p>
        </div>
        {novelId && (
          <Button onClick={handleAddSource} size="lg">
            <Plus className="h-4 w-4 mr-2" />
            {t("actions.addSource")}
          </Button>
        )}
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                placeholder={t("filters.searchPlaceholder")}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
            <Select value={statusFilter} onValueChange={(v) => setStatusFilter(v as FilterStatus)}>
              <SelectTrigger className="w-full sm:w-[200px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">{t("filters.all")}</SelectItem>
                <SelectItem value="enabled">{t("filters.enabled")}</SelectItem>
                <SelectItem value="disabled">{t("filters.disabled")}</SelectItem>
                <SelectItem value="failed">{t("filters.failed")}</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Sources List */}
      <div className="grid gap-4">
        {filteredSources.map((source) => (
          <Card key={source.id} className="overflow-hidden">
            <CardHeader className="pb-3">
              <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                  <CardTitle className="text-lg flex items-center gap-2">
                    {source.novelTitle}
                    {!source.enabled && (
                      <Badge variant="outline">{t("status.disabled")}</Badge>
                    )}
                  </CardTitle>
                  <CardDescription className="flex items-center gap-2 mt-1">
                    <a 
                      href={source.sourceUrl} 
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="flex items-center gap-1 hover:underline truncate"
                    >
                      <span className="truncate">{source.sourceUrl}</span>
                      <ExternalLink className="h-3 w-3 flex-shrink-0" />
                    </a>
                  </CardDescription>
                </div>
                {getSyncStatusBadge(source.syncStatus)}
              </div>
            </CardHeader>
            
            <CardContent className="space-y-4">
              {/* Stats Grid */}
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
                <div>
                  <p className="text-muted-foreground text-xs">{t("fields.lastSync")}</p>
                  <p className="font-medium">
                    {source.lastSyncTime
                      ? formatRelativeTime(source.lastSyncTime)
                      : t("status.never")}
                  </p>
                </div>
                <div>
                  <p className="text-muted-foreground text-xs">{t("fields.lastChapter")}</p>
                  <p className="font-medium">{source.lastSyncedChapter || "-"}</p>
                </div>
                <div>
                  <p className="text-muted-foreground text-xs">{t("fields.interval")}</p>
                  <p className="font-medium">{source.syncIntervalMinutes} {t("fields.minutes")}</p>
                </div>
                <div>
                  <p className="text-muted-foreground text-xs">{t("fields.failures")}</p>
                  <p className={`font-medium ${source.consecutiveFailures > 0 ? 'text-destructive' : ''}`}>
                    {source.consecutiveFailures}
                  </p>
                </div>
              </div>

              {/* Error Message */}
              {source.errorMessage && (
                <div className="flex gap-2 p-3 rounded-lg bg-destructive/10 border border-destructive/20">
                  <AlertCircle className="h-4 w-4 text-destructive flex-shrink-0 mt-0.5" />
                  <div className="text-sm">
                    <p className="font-medium text-destructive">{t("fields.error")}</p>
                    <p className="text-destructive/90">{source.errorMessage}</p>
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="flex flex-wrap gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleToggleEnabled(source)}
                >
                  {source.enabled ? (
                    <>
                      <Pause className="h-4 w-4 mr-2" />
                      {t("actions.disable")}
                    </>
                  ) : (
                    <>
                      <Play className="h-4 w-4 mr-2" />
                      {t("actions.enable")}
                    </>
                  )}
                </Button>
                
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleSync(source.id, false)}
                  disabled={syncing.has(source.id) || !source.enabled}
                >
                  {syncing.has(source.id) ? (
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  ) : (
                    <RefreshCw className="h-4 w-4 mr-2" />
                  )}
                  {t("actions.syncNow")}
                </Button>
                
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleSync(source.id, true)}
                  disabled={syncing.has(source.id) || !source.enabled}
                >
                  <RefreshCw className="h-4 w-4 mr-2" />
                  {t("actions.fullImport")}
                </Button>
                
                <Button
                  size="sm"
                  variant="destructive"
                  onClick={() => setDeleteConfirmId(source.id)}
                >
                  <Trash2 className="h-4 w-4 mr-2" />
                  {t("actions.delete")}
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}

        {/* Empty State */}
        {filteredSources.length === 0 && (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-12">
              {sources.length === 0 ? (
                <>
                  <p className="text-muted-foreground mb-4">{t("empty.noSources")}</p>
                  {novelId && (
                    <Button onClick={handleAddSource}>
                      <Plus className="h-4 w-4 mr-2" />
                      {t("actions.addFirstSource")}
                    </Button>
                  )}
                </>
              ) : (
                <p className="text-muted-foreground">{t("empty.noMatches")}</p>
              )}
            </CardContent>
          </Card>
        )}
      </div>

      {/* Dialogs */}
      <NovelSourceDialog
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        source={editingSource}
        novelId={novelId}
        onSuccess={loadSources}
      />

      <AlertDialog open={!!deleteConfirmId} onOpenChange={() => setDeleteConfirmId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{t("delete.title")}</AlertDialogTitle>
            <AlertDialogDescription>
              {t("delete.description")}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{t("delete.cancel")}</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => deleteConfirmId && handleDelete(deleteConfirmId)}
              className="bg-destructive hover:bg-destructive/90"
            >
              {t("delete.confirm")}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
