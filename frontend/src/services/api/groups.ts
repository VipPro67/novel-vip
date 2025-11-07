import type { PageResponse } from "@/models"
import type { ApiClient } from "../api-client"

export const createGroupsApi = (client: ApiClient) => ({
  async getAllGroups(
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

    return client.request<PageResponse<any>>(`/api/groups/me`)
  },

  async deleteGroup(groupId: string) {
    return client.request<void>(`/api/groups/${groupId}`, {
      method: "DELETE",
    })
  },

  async getGroupMembers(groupId: string) {
    return client.request<any[]>(`/api/groups/${groupId}/members`)
  },

  async sendMessage(groupId: string, content: string) {
    return client.request<any>("/api/messages", {
      method: "POST",
      body: JSON.stringify({ groupId, content }),
    })
  },

  async getUserGroups() {
    return client.request<any[]>("/api/groups/my-groups")
  },

  async createGroup(name: string, description?: string, isPrivate = false) {
    return client.request<any>("/api/groups", {
      method: "POST",
      body: JSON.stringify({ name, description, isPrivate }),
    })
  },

  async joinGroup(groupId: string) {
    return client.request<any>(`/api/groups/${groupId}/join`, {
      method: "POST",
    })
  },

  async leaveGroup(groupId: string) {
    return client.request<any>(`/api/groups/${groupId}/leave`, {
      method: "POST",
    })
  },

  async updateGroup(groupId: string, data: { name?: string; description?: string }) {
    return client.request<any>(`/api/groups/${groupId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  },

  async addGroupMember(groupId: string, userId: string) {
    return client.request<any>(`/api/groups/${groupId}/members/${userId}`, {
      method: "POST",
    })
  },

  async removeGroupMember(groupId: string, userId: string) {
    return client.request<any>(`/api/groups/${groupId}/members/${userId}`, {
      method: "DELETE",
    })
  },
})
