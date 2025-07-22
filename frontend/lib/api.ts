const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
  statusCode: number
}

export interface PageResponse<T> {
  totalPages: number
  totalElements: number
  pageNumber: number
  pageSize: number
  content: T[]
}

export interface User {
  id: string
  username: string
  email: string
  fullName?: string
  roles: string[]
}

export interface Novel {
  id: string
  title: string
  description: string
  author: string
  coverImage?: string
  status: string
  categories: Category[]
  totalChapters: number
  views: number
  rating: number
  updatedAt: string
}

export interface Chapter {
  id: string
  chapterNumber: number
  title: string
  novelId: string
  novelTitle: string
  createdAt: string
  updatedAt: string
}

export interface ChapterDetail extends Chapter {
  jsonUrl?: string
  audioUrl?: string
}

export interface Category {
  id: string
  name: string
  slug: string
  description?: string
}

export interface Comment {
  id: string
  content: string
  userId: string
  username: string
  novelId?: string
  chapterId?: string
  parentId?: string
  replies?: Comment[]
  createdAt: string
  updatedAt: string
}

export interface Bookmark {
  id: string
  userId: string
  chapterId: string
  novelId: string
  chapterTitle: string
  novelTitle: string
  note?: string
  progress: number
  createdAt: string
  updatedAt: string
}

export interface ReadingHistory {
  id: string
  userId: string
  novelId: string
  novelTitle: string
  novelCover?: string
  chapterId: string
  chapterTitle: string
  chapterNumber: number
  progress: number
  readingTime: number
  lastReadAt: string
  createdAt: string
}

export interface Rating {
  id: string
  userId: string
  username: string
  novelId: string
  novelTitle: string
  score: number
  review?: string
  createdAt: string
  updatedAt: string
}

export interface Review {
  id: string
  novelId: string
  novelTitle: string
  userId: string
  username: string
  userAvatar?: string
  title: string
  content: string
  rating: number
  helpfulVotes: number
  unhelpfulVotes: number
  edited: boolean
  verifiedPurchase: boolean
  createdAt: string
  updatedAt: string
}

export interface Notification {
  id: string
  userId: string
  title: string
  message: string
  read: boolean
  type: "SYSTEM" | "BOOK_UPDATE" | "CHAPTER_UPDATE" | "COMMENT" | "LIKE" | "FOLLOW" | "MESSAGE"
  createdAt: string
}

export interface ReaderSettings {
  id: string
  userId: string
  fontSize: number
  fontFamily: string
  lineHeight: number
  theme: string
  marginSize: number
  paragraphSpacing: number
  autoScroll: boolean
  autoScrollSpeed: number
  keepScreenOn: boolean
  showProgress: boolean
  showChapterTitle: boolean
  showTime: boolean
  showBattery: boolean
}

class ApiClient {
  private baseURL: string
  private token: string | null = null

  constructor(baseURL: string) {
    this.baseURL = baseURL
    if (typeof window !== "undefined") {
      this.token = localStorage.getItem("token")
    }
  }

  setToken(token: string) {
    this.token = token
    if (typeof window !== "undefined") {
      localStorage.setItem("token", token)
    }
  }

  clearToken() {
    this.token = null
    if (typeof window !== "undefined") {
      localStorage.removeItem("token")
    }
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
    const url = `${this.baseURL}${endpoint}`
    const headers: HeadersInit = {
      "Content-Type": "application/json",
      ...options.headers,
    }

    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`
    }

    const response = await fetch(url, {
      ...options,
      headers,
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    return response.json()
  }

  // Auth endpoints
  async login(email: string, password: string) {
    const response = await this.request<{
      id: string
      username: string
      email: string
      roles: string[]
      tokenType: string
      accessToken: string
    }>("/api/auth/signin", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    })

    if (response.success && response.data.accessToken) {
      this.setToken(response.data.accessToken)
    }

    return response
  }

  async register(username: string, email: string, password: string) {
    return this.request("/api/auth/signup", {
      method: "POST",
      body: JSON.stringify({ username, email, password }),
    })
  }

  async getUserProfile() {
    return this.request<User>("/api/users/profile")
  }

  async updateUserProfile(data: { fullName?: string; avatar?: string }) {
    return this.request<User>("/api/users/profile", {
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  async changePassword(oldPassword: string, newPassword: string, confirmPassword: string) {
    return this.request<void>("/api/users/change-password", {
      method: "PUT",
      body: JSON.stringify({ oldPassword, newPassword, confirmPassword }),
    })
  }

  // Novel endpoints
  async getNovels(
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

    return this.request<PageResponse<Novel>>(`/api/novels?${searchParams}`)
  }

  async getNovelById(id: string) {
    return this.request<Novel>(`/api/novels/${id}`)
  }

  async searchNovels(
    keyword: string,
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams({ keyword })
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value.toString())
      }
    })

    return this.request<PageResponse<Novel>>(`/api/novels/search?${searchParams}`)
  }

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
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value.toString())
      }
    })

    return this.request<PageResponse<Novel>>(`/api/novels/category/${categoryId}?${searchParams}`)
  }

  async getHotNovels(
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

    return this.request<PageResponse<Novel>>(`/api/novels/hot?${searchParams}`)
  }

  async getTopRatedNovels(
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

    return this.request<PageResponse<Novel>>(`/api/novels/top-rated?${searchParams}`)
  }

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

    return this.request<PageResponse<Novel>>(`/api/novels/latest-updates?${searchParams}`)
  }

  // Chapter endpoints
  async getChaptersByNovel(
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

    return this.request<PageResponse<Chapter>>(`/api/chapters/novel/${novelId}?${searchParams}`)
  }

  async getChapterById(id: string) {
    return this.request<ChapterDetail>(`/api/chapters/${id}`)
  }

  async getChapterByNumber(novelId: string, chapterNumber: number) {
    return this.request<ChapterDetail>(`/api/chapters/novel/${novelId}/chapter/${chapterNumber}`)
  }

  // Reading History endpoints
  async addReadingHistory(chapterId: string) {
    return this.request<ReadingHistory>(`/api/reading-history/chapter/${chapterId}`, {
      method: "POST",
    })
  }

  async getReadingHistory(
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

    return this.request<PageResponse<ReadingHistory>>(`/api/reading-history?${searchParams}`)
  }

  async getRecentlyRead(limit = 5) {
    return this.request<Novel[]>(`/api/reading-history/recent?limit=${limit}`)
  }

  // Favorites endpoints
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

    return this.request<PageResponse<Novel>>(`/api/favorites?${searchParams}`)
  }

  async addToFavorites(novelId: string) {
    return this.request<void>(`/api/favorites/${novelId}`, {
      method: "POST",
    })
  }

  async removeFromFavorites(novelId: string) {
    return this.request<void>(`/api/favorites/${novelId}`, {
      method: "DELETE",
    })
  }

  async checkFavoriteStatus(novelId: string) {
    return this.request<boolean>(`/api/favorites/${novelId}/status`)
  }

  // Bookmarks endpoints
  async getBookmarks(
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

    return this.request<PageResponse<Bookmark>>(`/api/bookmarks?${searchParams}`)
  }

  async createBookmark(data: {
    chapterId: string
    novelId: string
    note?: string
    progress?: number
  }) {
    return this.request<Bookmark>("/api/bookmarks", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async updateBookmark(
    id: string,
    data: {
      note?: string
      progress?: number
    },
  ) {
    return this.request<Bookmark>(`/api/bookmarks/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  async deleteBookmark(id: string) {
    return this.request<void>(`/api/bookmarks/${id}`, {
      method: "DELETE",
    })
  }

  // Ratings endpoints
  async rateNovel(novelId: string, score: number, review?: string) {
    return this.request<Rating>(`/api/ratings/novel/${novelId}`, {
      method: "POST",
      body: JSON.stringify({ score, review }),
    })
  }

  async getUserRating(novelId: string) {
    return this.request<Rating>(`/api/ratings/novel/${novelId}/user`)
  }

  // Comments endpoints
  async getNovelComments(
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

    return this.request<PageResponse<Comment>>(`/api/comments/novel/${novelId}?${searchParams}`)
  }

  async addComment(data: {
    content: string
    novelId?: string
    chapterId?: string
  }) {
    return this.request<Comment>("/api/comments", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  // Notifications endpoints
  async getNotifications(
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

    return this.request<PageResponse<Notification>>(`/api/notifications?${searchParams}`)
  }

  async markNotificationAsRead(notificationId: string) {
    return this.request<Notification>(`/api/notifications/${notificationId}/read`, {
      method: "PUT",
    })
  }

  async markAllNotificationsAsRead() {
    return this.request<void>("/api/notifications/read-all", {
      method: "PUT",
    })
  }

  async getUnreadNotificationCount() {
    return this.request<number>("/api/notifications/unread/count")
  }

  // Reader Settings endpoints
  async getReaderSettings() {
    return this.request<ReaderSettings>("/api/reader-settings")
  }

  async updateReaderSettings(settings: Partial<ReaderSettings>) {
    return this.request<ReaderSettings>("/api/reader-settings", {
      method: "PUT",
      body: JSON.stringify(settings),
    })
  }

  async getThemeOptions() {
    return this.request<string[]>("/api/reader-settings/themes")
  }

  async getFontOptions() {
    return this.request<string[]>("/api/reader-settings/fonts")
  }

  // Admin endpoints
  async getAllUsers(
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

    return this.request<PageResponse<User>>(`/api/users?${searchParams}`)
  }

  async updateUserRoles(userId: string, roles: string[]) {
    return this.request<User>(`/api/users/${userId}/roles`, {
      method: "PUT",
      body: JSON.stringify(roles),
    })
  }

  async deleteUser(userId: string) {
    return this.request<void>(`/api/users/${userId}`, {
      method: "DELETE",
    })
  }
}

export const api = new ApiClient(API_BASE_URL)
