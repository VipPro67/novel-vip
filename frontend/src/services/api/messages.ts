import type { PageResponse } from "@/models"
import type { ApiClient } from "../api-client"

export const createMessagesApi = (client: ApiClient) => ({
  async getAllMessages(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
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

    return client.request<PageResponse<any>>(`/api/messages/admin?${searchParams}`)
  },

  async deleteMessage(messageId: string) {
    return client.request<void>(`/api/messages/${messageId}`, {
      method: "DELETE",
    })
  },

  async getMessagesByGroup(
    groupId: string,
    params: {
      page?: number
      size?: number
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    const page = params.page ?? 0
    const size = params.size ?? 20

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())

    return client.request<PageResponse<any>>(`/api/messages/by-receiver-or-group/${groupId}?${searchParams}`)
  },
})
