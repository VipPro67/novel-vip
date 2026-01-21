import { ApiClient } from '../api-client'

export type SyncStatus = 'IDLE' | 'SYNCING' | 'SUCCESS' | 'FAILED'
export type SourcePlatform = 'SHUBA69' | 'OTHER'

export interface NovelSource {
  id: string
  novelId: string
  novelTitle: string
  sourceUrl: string
  sourceId?: string
  sourcePlatform: SourcePlatform
  enabled: boolean
  lastSyncedChapter?: number
  lastSyncTime?: string
  syncStatus: SyncStatus
  nextSyncTime?: string
  syncIntervalMinutes: number
  errorMessage?: string
  consecutiveFailures: number
  createdAt: string
  updatedAt: string
}

export interface CreateNovelSourceDTO {
  novelId: string
  sourceUrl: string
  sourceId?: string
  sourcePlatform?: string
  syncIntervalMinutes?: number
}

export interface UpdateNovelSourceDTO {
  enabled?: boolean
  syncIntervalMinutes?: number
  sourceId?: string
}

export interface ShubaImportRequestDTO {
  startChapter?: number
  endChapter?: number
  fullImport?: boolean
}

export function createNovelSourcesApi(client: ApiClient) {
  return {
    // Create a new novel source
    createNovelSource: (data: CreateNovelSourceDTO) =>
      client.post<NovelSource>('/api/admin/novel-sources', data),

    // Quick import (create + trigger import)
    quickImport: (data: CreateNovelSourceDTO) =>
      client.post<string>('/api/admin/novel-sources/import', data),

    // Get all novel sources
    getAllNovelSources: () =>
      client.get<NovelSource[]>('/api/admin/novel-sources'),

    // Get novel sources for a specific novel
    getNovelSourcesByNovelId: (novelId: string) =>
      client.get<NovelSource[]>(`/api/admin/novel-sources/novel/${novelId}`),

    // Get a specific novel source
    getNovelSource: (id: string) =>
      client.get<NovelSource>(`/api/admin/novel-sources/${id}`),

    // Update novel source
    updateNovelSource: (id: string, data: UpdateNovelSourceDTO) =>
      client.put<NovelSource>(`/api/admin/novel-sources/${id}`, data),

    // Delete novel source
    deleteNovelSource: (id: string) =>
      client.delete<void>(`/api/admin/novel-sources/${id}`),

    // Trigger manual sync
    triggerSync: (id: string, request?: ShubaImportRequestDTO) =>
      client.post<string>(`/api/admin/novel-sources/${id}/sync`, request || {}),
  }
}
