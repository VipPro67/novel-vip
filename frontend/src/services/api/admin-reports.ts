import type { PageResponse, Report } from "@/models"
import type { ApiClient } from "../api-client"

export const createAdminReportsApi = (client: ApiClient) => ({
  async getAllReports(
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

    return client.request<PageResponse<Report>>(`/api/admin/reports?${searchParams}`)
  },

  async updateReportStatus(reportId: string, status: string) {
    return client.request<Report>(`/api/admin/reports/${reportId}/status`, {
      method: "PUT",
      body: JSON.stringify({ status }),
    })
  },

  async resolveReport(reportId: string, action: string) {
    return client.request<Report>(`/api/admin/reports/${reportId}/resolve`, {
      method: "POST",
      body: JSON.stringify({ action }),
    })
  },
})
