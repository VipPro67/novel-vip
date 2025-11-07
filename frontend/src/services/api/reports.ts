import type { PageResponse, Report } from "@/models"
import type { FeatureRequest } from "@/models/feature-request"
import type { ApiClient } from "../api-client"

export const createReportsApi = (client: ApiClient) => ({
  async createReport(data: {
    reportType: "NOVEL" | "CHAPTER" | "COMMENT" | "REVIEW" | "USER"
    targetId: string
    reason: "SPAM" | "INAPPROPRIATE_CONTENT" | "COPYRIGHT_VIOLATION" | "HARASSMENT" | "MISINFORMATION" | "OTHER"
    description: string
  }) {
    return client.request<Report>("/api/reports", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  async getMyReports(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
      status?: string
      reportType?: string
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
    if (params.reportType) {
      searchParams.append("reportType", params.reportType)
    }

    return client.request<PageResponse<Report>>(`/api/reports/my-reports?${searchParams}`)
  },

  async getFeatureRequests(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
      status?: string
      category?: string
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
    if (params.category) {
      searchParams.append("category", params.category)
    }

    return client.request<PageResponse<FeatureRequest>>(`/api/feature-requests?${searchParams}`)
  },

  async createFeatureRequest(data: { title: string; description: string; category: string }) {
    return client.request<FeatureRequest>("/api/feature-requests", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  async voteFeatureRequest(featureRequestId: string) {
    return client.request<FeatureRequest>(`/api/feature-requests/${featureRequestId}/vote`, {
      method: "POST",
    })
  },

  async unvoteFeatureRequest(featureRequestId: string) {
    return client.request<FeatureRequest>(`/api/feature-requests/${featureRequestId}/vote`, {
      method: "DELETE",
    })
  },
})
