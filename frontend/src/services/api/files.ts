import type { ApiResponse, FileMetadata, PageResponse } from "@/models"
import type { ApiClient } from "../api-client"

const withAuthHeaders = (token: string | null) => {
  const headers: HeadersInit = {}
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  return headers
}

export const createFilesApi = (client: ApiClient) => ({
  async uploadFile(file: File, type?: string): Promise<ApiResponse<FileMetadata>> {
    const formData = new FormData()
    formData.append("file", file)

    if (type) {
      formData.append("type", type)
    }

    const response = await fetch(`${client.baseURL}/api/files/upload`, {
      method: "POST",
      headers: withAuthHeaders(client.getToken()),
      body: formData,
    })

    if (!response.ok) {
      throw new Error(`Upload failed: ${response.statusText}`)
    }

    return response.json()
  },

  async uploadEpub(file: File): Promise<ApiResponse<FileMetadata>> {
    const formData = new FormData()
    formData.append("file", file)
    formData.append("type", "NOVEL_EPUB")

    try {
      const response = await fetch(`${client.baseURL}/api/files/upload`, {
        method: "POST",
        headers: withAuthHeaders(client.getToken()),
        body: formData,
      })

      if (!response.ok) {
        throw new Error(`Upload failed: ${response.statusText}`)
      }

      return response.json()
    } catch (error) {
      console.error("EPUB Upload Error:", error)
      throw error
    }
  },

  async addChaptersFromEpub(id: string | undefined, epubFile: File): Promise<ApiResponse<FileMetadata>> {
    const formData = new FormData()
    formData.append("epub", epubFile)

    try {
      const response = await fetch(`${client.baseURL}/api/novels/${id}/import-epub`, {
        method: "POST",
        headers: withAuthHeaders(client.getToken()),
        body: formData,
      })

      if (!response.ok) {
        throw new Error(`Upload failed: ${response.statusText}`)
      }

      return response.json()
    } catch (error) {
      console.error("EPUB Upload Error:", error)
      throw error
    }
  },

  async getAllFiles(
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
    const sortBy = params.sortBy ?? "uploadedAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return client.request<PageResponse<FileMetadata>>(`/api/files?${searchParams}`)
  },

  async deleteFile(fileId: string) {
    return client.request<void>(`/api/files/${fileId}`, {
      method: "DELETE",
    })
  },
})
