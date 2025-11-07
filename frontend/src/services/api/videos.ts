import type { CreateVideoPayload, PageResponse, Video } from "@/models"
import type { ApiClient } from "../api-client"

export const createVideosApi = (client: ApiClient) => ({
  async getVideos(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
      search?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    const page = params.page ?? 0
    const size = params.size ?? 12
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    if (params.search && params.search.trim().length > 0) {
      searchParams.append("search", params.search.trim())
    }

    return client.request<PageResponse<Video>>(`/api/videos?${searchParams.toString()}`)
  },

  async getVideo(videoId: string) {
    return client.request<Video>(`/api/videos/${videoId}`)
  },

  async createVideo(payload: CreateVideoPayload) {
    return client.request<Video>("/api/videos", {
      method: "POST",
      body: JSON.stringify(payload),
    })
  },
})
