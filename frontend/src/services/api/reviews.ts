import type { PageResponse, Review } from "@/models"
import type { ApiClient } from "../api-client"

export const createReviewsApi = (client: ApiClient) => ({
  async getNovelReviews(
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

    return client.request<PageResponse<Review>>(`/api/reviews/novel/${novelId}?${searchParams}`)
  },

  async createReview(data: { novelId: string; title: string; content: string; rating: number }) {
    return client.request<Review>("/api/reviews", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  async updateReview(
    reviewId: string,
    data: {
      title: string
      content: string
      rating: number
    },
  ) {
    return client.request<Review>(`/api/reviews/${reviewId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  },

  async deleteReview(reviewId: string) {
    return client.request<void>(`/api/reviews/${reviewId}`, {
      method: "DELETE",
    })
  },
})
