import type { Novel, PageResponse } from "@/models"
import type { ApiClient } from "../api-client"

export const createFavoritesApi = (client: ApiClient) => ({
  async getFavorites(
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

    return client.request<PageResponse<Novel>>(`/api/favorites?${searchParams}`)
  },

  async addToFavorites(novelId: string) {
    return client.request<void>(`/api/favorites/${novelId}`, {
      method: "POST",
    })
  },

  async removeFromFavorites(novelId: string) {
    return client.request<void>(`/api/favorites/${novelId}`, {
      method: "DELETE",
    })
  },

  async checkFavoriteStatus(novelId: string) {
    return client.request<boolean>(`/api/favorites/${novelId}/status`)
  },
})
