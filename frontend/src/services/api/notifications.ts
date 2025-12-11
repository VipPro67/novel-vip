import type { Notification, PageResponse } from "@/models"
import type { ApiClient } from "../api-client"

export const createNotificationsApi = (client: ApiClient) => ({
  async getNotifications(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
      read?: boolean
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value.toString())
      }
    })

    return client.request<PageResponse<Notification>>(`/api/notifications?${searchParams}`)
  },

  async markNotificationAsRead(notificationId: string) {
    return client.request<void>(`/api/notifications/${notificationId}/read`, {
      method: "PUT",
    })
  },

  async markAllNotificationsAsRead() {
    return client.request<void>("/api/notifications/read-all", {
      method: "PUT",
    })
  },

  async getUnreadNotificationCount() {
    return client.request<number>("/api/notifications/unread-count")
  },
})
