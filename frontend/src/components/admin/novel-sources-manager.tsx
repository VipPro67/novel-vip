"use client"

import { useState, useEffect } from "react"
import { api } from "@/services/api"
import { NovelSource } from "@/services/api/novel-sources"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { toast } from "@/hooks/use-toast"
import { Loader2, Play, Pause, Trash2, Plus, RefreshCw } from "lucide-react"

export function NovelSourcesManager({ novelId }: { novelId?: string }) {
  const [sources, setSources] = useState<NovelSource[]>([])
  const [loading, setLoading] = useState(true)
  const [syncing, setSyncing] = useState<string | null>(null)
  const [showAddForm, setShowAddForm] = useState(false)
  const [formData, setFormData] = useState({
    sourceUrl: "",
    syncIntervalMinutes: 60,
  })

  useEffect(() => {
    loadSources()
  }, [novelId])

  const loadSources = async () => {
    try {
      setLoading(true)
      const response = novelId
        ? await api.getNovelSourcesByNovelId(novelId)
        : await api.getAllNovelSources()
      setSources(response.data || [])
    } catch (error) {
      console.error("Failed to load sources:", error)
      toast({
        title: "Error",
        description: "Failed to load novel sources",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleAddSource = async () => {
    if (!novelId) return

    try {
      await api.quickImport({
        novelId,
        sourceUrl: formData.sourceUrl,
        syncIntervalMinutes: formData.syncIntervalMinutes,
      })
      toast({
        title: "Success",
        description: "Novel source added and import started",
      })
      setShowAddForm(false)
      setFormData({ sourceUrl: "", syncIntervalMinutes: 60 })
      loadSources()
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to add novel source",
        variant: "destructive",
      })
    }
  }

  const handleToggleEnabled = async (source: NovelSource) => {
    try {
      await api.updateNovelSource(source.id, {
        enabled: !source.enabled,
      })
      toast({
        title: "Success",
        description: source.enabled ? "Source disabled" : "Source enabled",
      })
      loadSources()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to update source",
        variant: "destructive",
      })
    }
  }

  const handleSync = async (sourceId: string) => {
    try {
      setSyncing(sourceId)
      await api.triggerSync(sourceId, { fullImport: false })
      toast({
        title: "Success",
        description: "Sync job started",
      })
      loadSources()
    } catch (error: any) {
      toast({
        title: "Error",
        description: error.message || "Failed to start sync",
        variant: "destructive",
      })
    } finally {
      setSyncing(null)
    }
  }

  const handleDelete = async (sourceId: string) => {
    if (!confirm("Are you sure you want to delete this source?")) return

    try {
      await api.deleteNovelSource(sourceId)
      toast({
        title: "Success",
        description: "Source deleted",
      })
      loadSources()
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete source",
        variant: "destructive",
      })
    }
  }

  const getSyncStatusBadge = (status: NovelSource["syncStatus"]) => {
    const variants = {
      IDLE: "secondary",
      SYNCING: "default",
      SUCCESS: "default",
      FAILED: "destructive",
    } as const

    return (
      <Badge variant={variants[status] || "secondary"}>
        {status}
      </Badge>
    )
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center p-8">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold">Novel Sources</h2>
        {novelId && (
          <Button onClick={() => setShowAddForm(!showAddForm)}>
            <Plus className="h-4 w-4 mr-2" />
            Add Source
          </Button>
        )}
      </div>

      {showAddForm && (
        <Card>
          <CardHeader>
            <CardTitle>Add Novel Source</CardTitle>
            <CardDescription>
              Import chapters from 69shuba.com
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="sourceUrl">Source URL</Label>
              <Input
                id="sourceUrl"
                placeholder="https://www.69shuba.com/book/12345.html"
                value={formData.sourceUrl}
                onChange={(e) =>
                  setFormData({ ...formData, sourceUrl: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="interval">Sync Interval (minutes)</Label>
              <Input
                id="interval"
                type="number"
                value={formData.syncIntervalMinutes}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    syncIntervalMinutes: parseInt(e.target.value),
                  })
                }
              />
            </div>
            <div className="flex gap-2">
              <Button onClick={handleAddSource}>Add & Import</Button>
              <Button variant="outline" onClick={() => setShowAddForm(false)}>
                Cancel
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid gap-4">
        {sources.map((source) => (
          <Card key={source.id}>
            <CardHeader>
              <div className="flex items-start justify-between">
                <div>
                  <CardTitle className="text-lg">
                    {source.novelTitle}
                  </CardTitle>
                  <CardDescription className="break-all">
                    {source.sourceUrl}
                  </CardDescription>
                </div>
                {getSyncStatusBadge(source.syncStatus)}
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Last Synced:</span>
                  <span>
                    {source.lastSyncTime
                      ? new Date(source.lastSyncTime).toLocaleString()
                      : "Never"}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Last Chapter:</span>
                  <span>{source.lastSyncedChapter || "None"}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">Interval:</span>
                  <span>{source.syncIntervalMinutes} minutes</span>
                </div>
                {source.errorMessage && (
                  <div className="text-destructive text-xs mt-2">
                    Error: {source.errorMessage}
                  </div>
                )}
              </div>

              <div className="flex gap-2 mt-4">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleToggleEnabled(source)}
                >
                  {source.enabled ? (
                    <>
                      <Pause className="h-4 w-4 mr-2" />
                      Disable
                    </>
                  ) : (
                    <>
                      <Play className="h-4 w-4 mr-2" />
                      Enable
                    </>
                  )}
                </Button>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleSync(source.id)}
                  disabled={syncing === source.id}
                >
                  {syncing === source.id ? (
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  ) : (
                    <RefreshCw className="h-4 w-4 mr-2" />
                  )}
                  Sync Now
                </Button>
                <Button
                  size="sm"
                  variant="destructive"
                  onClick={() => handleDelete(source.id)}
                >
                  <Trash2 className="h-4 w-4 mr-2" />
                  Delete
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}

        {sources.length === 0 && (
          <Card>
            <CardContent className="flex flex-col items-center justify-center py-8">
              <p className="text-muted-foreground">No sources configured</p>
              {novelId && (
                <Button
                  className="mt-4"
                  onClick={() => setShowAddForm(true)}
                >
                  Add First Source
                </Button>
              )}
            </CardContent>
          </Card>
        )}
      </div>
    </div>
  )
}
