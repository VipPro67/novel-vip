import type { Comment, PageResponse } from "@/models"
import type { ApiClient } from "../api-client"

export const createCommentsApi = (client: ApiClient) => ({
  async getNovelComments(
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

    return client.request<PageResponse<Comment>>(`/api/comments/novel/${novelId}?${searchParams}`)
  },

  async getChapterComments(
    chapterId: string,
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

    return client.request<PageResponse<Comment>>(`/api/comments/chapter/${chapterId}?${searchParams}`)
  },

  async addComment(data: { content: string; novelId?: string; chapterId?: string, parentId?: string}) {
    return client.request<Comment>("/api/comments", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  async updateComment(commentId: string, data: { content: string }) {
    return client.request<Comment>(`/api/comments/${commentId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  },

  async deleteComment(commentId: string) {
    return client.request<void>(`/api/comments/${commentId}`, {
      method: "DELETE",
    })
  },
})
