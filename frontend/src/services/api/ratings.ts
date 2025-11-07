import type { Rating } from "@/models"
import type { ApiClient } from "../api-client"

export const createRatingsApi = (client: ApiClient) => ({
  async rateNovel(novelId: string, score: number, review?: string) {
    return client.request<Rating>(`/api/ratings/novel/${novelId}`, {
      method: "POST",
      body: JSON.stringify({ score, review }),
    })
  },

  async getUserRating(novelId: string) {
    return client.request<Rating>(`/api/ratings/novel/${novelId}/user`)
  },
})
