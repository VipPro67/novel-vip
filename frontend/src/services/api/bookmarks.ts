import type { Bookmark, PageResponse } from "@/models"
import type { ApiClient } from "../api-client"

export const createBookmarksApi = (client: ApiClient) => ({
  async getBookmarks(
    params: {
      page?: number
      size?: number
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value.toString())
      }
    })

    return client.request<PageResponse<Bookmark>>(`/api/bookmarks?${searchParams}`)
  },

  async createBookmark(data: { chapterId: string; novelId: string; note?: string; progress?: number }) {
    return client.request<Bookmark>("/api/bookmarks", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  async updateBookmark(
    id: string,
    data: {
      note?: string
      progress?: number
    },
  ) {
    return client.request<Bookmark>(`/api/bookmarks/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  },

  async deleteBookmark(id: string) {
    return client.request<void>(`/api/bookmarks/${id}`, {
      method: "DELETE",
    })
  },
})
