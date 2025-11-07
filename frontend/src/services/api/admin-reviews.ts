import type { PageResponse, Review } from "@/models"
import type { ApiClient } from "../api-client"

export const createAdminReviewsApi = (client: ApiClient) => ({
  async getAllReviews(
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

    return client.request<PageResponse<Review>>(`/api/reviews?${searchParams}`)
  },

  async flagReview(reviewId: string, reason: string) {
    return client.request<Review>(`/api/reviews/${reviewId}/flag`, {
      method: "POST",
      body: JSON.stringify({ reason }),
    })
  },
})
