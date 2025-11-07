import type { Notification, PageResponse } from "@/models"
import type { ApiClient } from "../api-client"

export const createAdminNotificationsApi = (client: ApiClient) => ({
  async getAllNotifications(
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

    return client.request<PageResponse<Notification>>(`/api/notifications?${searchParams}`)
  },

  async createSystemNotification(data: { title: string; content: string; type: "INFO" | "WARNING" | "ALERT" }) {
    return client.request<Notification>("/api/admin/notifications", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  async updateNotification(
    notificationId: string,
    data: {
      title?: string
      content?: string
      type?: "INFO" | "WARNING" | "ALERT"
      status?: "ACTIVE" | "INACTIVE"
    },
  ) {
    return client.request<Notification>(`/api/admin/notifications/${notificationId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  },

  async deleteNotification(notificationId: string) {
    return client.request<void>(`/api/admin/notifications/${notificationId}`, {
      method: "DELETE",
    })
  },
})
