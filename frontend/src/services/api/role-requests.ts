import type { PageResponse, RoleRequest } from "@/models"
import type { ApiClient } from "../api-client"

export const createRoleRequestsApi = (client: ApiClient) => ({
  async requestRole(requestedRole: string, reason: string) {
    return client.request<RoleRequest>("/api/role-approval/request", {
      method: "POST",
      body: JSON.stringify({ requestedRole, reason }),
    })
  },

  async getPendingRoleRequests(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()

    const page = params.page ?? 0
    const size = params.size ?? 10
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return client.request<PageResponse<RoleRequest>>(`/api/role-approval/pending?${searchParams}`)
  },

  async getMyRoleRequests(
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

    return client.request<PageResponse<RoleRequest>>(`/api/role-approval/my-requests?${searchParams}`)
  },

  async approveRoleRequest(requestId: string) {
    return client.request<RoleRequest>(`/api/role-approval/approve/${requestId}`, {
      method: "POST",
    })
  },

  async rejectRoleRequest(requestId: string) {
    return client.request<RoleRequest>(`/api/role-approval/reject/${requestId}`, {
      method: "POST",
    })
  },
})
