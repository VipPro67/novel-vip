import type { ApiResponse, Category, Genre, Novel, PageResponse, Tag } from "@/models"
import type { ApiClient } from "../api-client"

export const createNovelApi = (client: ApiClient) => ({
  async getNovels(
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
    const size = params.size ?? 20
    const sortBy = params.sortBy ?? "updatedAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    if (params.search && params.search.trim()) {
      searchParams.append("search", params.search.trim())
    }

    return client.request<PageResponse<Novel>>(`/api/novels?${searchParams}`)
  },

  async getNovelById(id: string) {
    return client.request<Novel>(`/api/novels/${id}`)
  },

  async getNovelBySlug(slug: string) {
    return client.request<Novel>(`/api/novels/slug/${slug}`)
  },

  async createNovel(data: {
    title: string
    slug: string
    description: string
    author: string
    coverImage?: string
    status: string
    categories: string[]
    genres?: string[]
    tags?: string[]
  }) {
    return client.request<Novel>("/api/novels", {
      method: "POST",
      body: JSON.stringify(data),
    })
  },

  async updateNovel(
    id: string,
    data:
      | {
          title: string
          slug: string
          description: string
          author: string
          status: string
          categories: string[]
          genres?: string[]
          tags?: string[]
        }
      | FormData,
  ) {
    const isFormData = data instanceof FormData
    const options: RequestInit = {
      method: "PUT",
    }

    if (isFormData) {
      options.body = data
    } else {
      options.body = JSON.stringify(data)
    }

    return client.request<Novel>(`/api/novels/${id}`, options)
  },

  async updateNovelCover(id: string, coverImage: File): Promise<ApiResponse<Novel>> {
    const formData = new FormData()
    formData.append("coverImage", coverImage)

    const headers: HeadersInit = {}
    const token = client.getToken()
    if (token) {
      headers.Authorization = `Bearer ${token}`
    }

    try {
      const response = await fetch(`${client.baseURL}/api/novels/${id}/cover`, {
        method: "PUT",
        headers,
        body: formData,
      })

      if (!response.ok) {
        throw new Error(`Cover update failed: ${response.statusText}`)
      }

      return response.json()
    } catch (error) {
      console.error("Cover Update Error:", error)
      throw error
    }
  },

  async deleteNovel(id: string) {
    return client.request<void>(`/api/novels/${id}`, {
      method: "DELETE",
    })
  },

  async searchNovels(
    params: {
      keyword?: string
      title?: string
      author?: string
      category?: string
      genre?: string
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    const page = params.page ?? 0
    const size = params.size ?? 20
    const sortBy = params.sortBy ?? "updatedAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    if (params.keyword) searchParams.append("keyword", params.keyword)
    if (params.title) searchParams.append("title", params.title)
    if (params.author) searchParams.append("author", params.author)
    if (params.category) searchParams.append("category", params.category)
    if (params.genre) searchParams.append("genre", params.genre)

    return client.request<PageResponse<Novel>>(`/api/novels/search?${searchParams}`)
  },

  async getCategories() {
    return client.request<Category[]>("/api/novels/categories")
  },

  async createCategory(name: string, description?: string) {
    return client.request<Category>("/api/novels/categories", {
      method: "POST",
      body: JSON.stringify({ name, description }),
    })
  },

  async updateCategory(id: string, name: string, description?: string) {
    return client.request<Category>(`/api/novels/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify({ name, description }),
    })
  },

  async getGenres() {
    return client.request<Genre[]>("/api/novels/genres")
  },

  async createGenre(name: string, description?: string) {
    return client.request<Genre>("/api/novels/genres", {
      method: "POST",
      body: JSON.stringify({ name, description }),
    })
  },

  async updateGenre(id: string, name: string, description?: string) {
    return client.request<Genre>(`/api/novels/genres/${id}`, {
      method: "PUT",
      body: JSON.stringify({ name, description }),
    })
  },

  async getTags() {
    return client.request<Tag[]>("/api/novels/tags")
  },

  async createTag(name: string, description?: string) {
    return client.request<Tag>("/api/novels/tags", {
      method: "POST",
      body: JSON.stringify({ name, description }),
    })
  },

  async updateTag(id: string, name: string, description?: string) {
    return client.request<Tag>(`/api/novels/tags/${id}`, {
      method: "PUT",
      body: JSON.stringify({ name, description }),
    })
  },

  async getNovelsByCategory(
    categoryId: string,
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
    const sortBy = params.sortBy ?? "updatedAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return client.request<PageResponse<Novel>>(`/api/novels/category/${categoryId}?${searchParams}`)
  },

  async getHotNovels(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    const page = params.page ?? 0
    const size = params.size ?? 10
    const sortBy = params.sortBy ?? "totalViews"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return client.request<PageResponse<Novel>>(`/api/novels/hot?${searchParams}`)
  },

  async getTopRatedNovels(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    const page = params.page ?? 0
    const size = params.size ?? 10
    const sortBy = params.sortBy ?? "rating"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return client.request<PageResponse<Novel>>(`/api/novels/top-rated?${searchParams}`)
  },

  async getHotNovelsByPeriod(period: "weekly" | "monthly" | "all-time") {
    return client.request<Novel[]>(`/api/novels/hot/${period}`)
  },

  async getTopRatedNovelsByPeriod(period: "weekly" | "monthly" | "all-time") {
    return client.request<Novel[]>(`/api/novels/top-rated/${period}`)
  },

  async getLatestNovels(
    params: {
      page?: number
      size?: number
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value.toString())
      }
    })

    return client.request<PageResponse<Novel>>(`/api/novels/latest-updates?${searchParams}`)
  },

  async getRecentlyRead(size = 6) {
    return client.request<Novel[]>(`/api/reading-history`)
  },
})
