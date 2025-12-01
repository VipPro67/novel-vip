import type { Chapter, ChapterDetail, FileMetadata, PageResponse, ReadingHistory } from "@/models"
import type { ApiClient } from "../api-client"

export const createChapterApi = (client: ApiClient) => ({
  async getChaptersByNovel(
    novelId: string,
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value.toString())
      }
    })

    return client.request<PageResponse<Chapter>>(`/api/chapters/novel/${novelId}?${searchParams.toString()}`)
  },

  async getChapterById(id: string) {
    return client.request<ChapterDetail>(`/api/chapters/${id}`)
  },

  async getChapterByNumber(novelId: string, chapterNumber: number) {
    return client.request<ChapterDetail>(`/api/chapters/novel/${novelId}/chapter/${chapterNumber}`)
  },

  async getChapterByNumber2(slug: string, chapterNumber: number) {
    return client.request<ChapterDetail>(`/api/chapters/novel/slug/${slug}/chapter/${chapterNumber}`)
  },

  async createChapter(data: {
    title: string
    chapterNumber: number
    contentHtml?: string
    content?: string
    format?: "HTML" | "TEXT"
    novelId: string
  }) {
    return client.request<Chapter>("/api/chapters", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  async updateChapter(
    id: string,
    data: {
      title: string
      chapterNumber: number
      content?: string
      contentHtml?: string
      format?: "HTML" | "TEXT"
      novelId: string
    },
  ) {
    return client.request<Chapter>(`/api/chapters/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  },

  async reindexChapter(id: string) {
    return client.request<void>(`/api/chapters/${id}/reindex`, {
      method: "POST",
    })
  },

  async deleteChapter(id: string) {
    return client.request<void>(`/api/chapters/${id}`, {
      method: "DELETE",
    })
  },

  async getChapterJsonMetadata(chapterId: string) {
    return client.request<FileMetadata>(`/api/chapters/${chapterId}/json-metadata`)
  },

  async getChapterAudioMetadata(chapterId: string) {
    return client.request<FileMetadata>(`/api/chapters/${chapterId}/audio-metadata`)
  },

  async getChapterAudio(chapterId: string) {
    return client.request<ChapterDetail>(`/api/chapters/${chapterId}/audio`)
  },

  async updateReadingProgress(novelId: string, lastReadChapterIndex: number) {
    return client.request<ReadingHistory>(
      `/api/reading-history/novel/${novelId}?lastReadChapterIndex=${lastReadChapterIndex}`,
      {
        method: "POST",
      },
    )
  },

  async getReadingHistory(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value.toString())
      }
    })

    return client.request<PageResponse<ReadingHistory>>(`/api/reading-history?${searchParams}`)
  },
})
