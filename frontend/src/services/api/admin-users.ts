import type { PageResponse, User } from "@/models"
import type { ApiClient } from "../api-client"

export const createAdminUsersApi = (client: ApiClient) => ({
  async getAllUsers(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
      search?: string
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

    if (params.search && params.search.trim()) {
      searchParams.append("search", params.search.trim())
    }

    return client.request<PageResponse<User>>(`/api/users?${searchParams}`)
  },

  async updateUserRoles(userId: string, roles: string[]) {
    return client.request<User>(`/api/users/${userId}/roles`, {
      method: "PUT",
      body: JSON.stringify({ roles }),
    })
  },

  async deleteUser(userId: string) {
    return client.request<void>(`/api/users/${userId}`, {
      method: "DELETE",
    })
  },
})
