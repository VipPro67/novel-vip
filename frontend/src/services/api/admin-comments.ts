import type { Comment, PageResponse } from "@/models"
import type { ApiClient } from "../api-client"

export const createAdminCommentsApi = (client: ApiClient) => ({
  async getAllComments(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
      status?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    const page = params.page ?? 0
    const size = params.size ?? 20
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    if (params.status) {
      searchParams.append("status", params.status)
    }

    return client.request<PageResponse<Comment>>(`/api/admin/comments?${searchParams}`)
  },

  async approveComment(commentId: string) {
    return client.request<Comment>(`/api/admin/comments/${commentId}/approve`, {
      method: "POST",
    })
  },

  async rejectComment(commentId: string) {
    return client.request<Comment>(`/api/admin/comments/${commentId}/reject`, {
      method: "POST",
    })
  },
})
