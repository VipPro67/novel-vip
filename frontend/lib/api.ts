const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081"

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
  slug: string
  author: string
  imageUrl: string
  status: string
  categories?: Category[]
  genres?: Genre[]
  tags?: Tag[]
  totalChapters: number
  totalViews: number
  monthlyViews: number
  dailyViews: number
  rating: number
  updatedAt: string
}

export interface FileMetadata {
  id: string
  contentType: string
  publicId: string
  fileUrl: string
  uploadedAt: string
  lastModifiedAt: string
  fileName: string
  type: string
  size: number
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
  description?: string
}

export interface Genre {
  id: string
  name: string
  description?: string
}

export interface Tag {
  id: string
  name: string
  description?: string
}

export interface Comment {
  id: string
  content: string
  userId: string
  username: string
  novelId?: string
  chapterId?: string
  parentId?: string | null
  replies?: Comment[]
  createdAt: string
  updatedAt: string
  edited?: boolean
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
  referenceId?: string
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
  textColor: string
  backgroundColor: string
  audioEnabled: boolean
  audioAutoNextChapter: boolean
  audioSpeed: number
}

export enum ERole {
  USER = 0,
  MODERATOR = 1,
  ADMIN = 2,
  AUTHOR = 3,
}

export interface RoleRequest {
  id: string
  userId: string
  username: string
  email: string
  requestedRole: ERole
  reason: string
  status: "PENDING" | "APPROVED" | "REJECTED"
  createdAt: string
  updatedAt: string
  processedBy?: string
  rejectReason?: string
}

export interface Report {
  id: string
  reportType: "NOVEL" | "CHAPTER" | "COMMENT" | "REVIEW" | "USER"
  targetId: string
  reason: "SPAM" | "INAPPROPRIATE_CONTENT" | "COPYRIGHT_VIOLATION" | "HARASSMENT" | "MISINFORMATION" | "OTHER"
  description: string
  status: "PENDING" | "REVIEWED" | "RESOLVED" | "DISMISSED"
  reportedBy: string
  createdAt: string
  updatedAt: string
}

export interface FeatureRequest {
  id: string
  title: string
  description: string
  status: "PENDING" | "UNDER_REVIEW" | "PLANNED" | "IN_PROGRESS" | "COMPLETED" | "REJECTED"
  category: string
  priority: "LOW" | "MEDIUM" | "HIGH"
  voteCount: number
  hasVoted: boolean
  createdBy: string
  createdByUsername: string
  createdAt: string
  updatedAt: string
}

class ApiClient {

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

  getToken(): string | null {
    return this.token
  }

  async request<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
    const url = `${this.baseURL}${endpoint}`

    const headers: HeadersInit = { ...options.headers }

    const isFormData = options.body instanceof FormData
    if (!isFormData) {
      headers["Content-Type"] = "application/json"
    }

    // Add Authorization header if token exists
    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      })

      console.log(`API Response: ${response.status} ${response.statusText}`)

      if (!response.ok) {
        if (response.status === 401) {
          this.clearToken()
          throw new Error("Authentication failed. Please login again.")
        } else if (response.status === 403) {
          throw new Error("Access denied. Insufficient permissions.")
        } else if (response.status === 404) {
          throw new Error("Resource not found.")
        } else if (response.status >= 500) {
          throw new Error("Server error. Please try again later.")
        } else {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
      }

      const data = await response.json()
      return data
    } catch (error) {
      console.error("API Request Error:", error)
      throw error
    }
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

  async register(username: string, email: string, password: string): Promise<ApiResponse<string>> {
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
      search?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()

    // Set default values
    const page = params.page ?? 0
    const size = params.size ?? 20
    const sortBy = params.sortBy ?? "updatedAt"
    const sortDir = params.sortDir ?? "desc"

    // Add pagination parameters
    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    // Add search parameter if provided
    if (params.search && params.search.trim()) {
      searchParams.append("search", params.search.trim())
    }

    return this.request<PageResponse<Novel>>(`/api/novels?${searchParams}`)
  }

  async getNovelById(id: string) {
    return this.request<Novel>(`/api/novels/${id}`)
  }

  async getNovelBySlug(slug: string) {
    return this.request<Novel>(`/api/novels/slug/${slug}`)
  }

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
    return this.request<Novel>("/api/novels", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async updateNovel(
    id: string,
    data: {
      title: string
      slug: string
      description: string
      author: string
      coverImage?: string
      status: string
      categories: string[]
      genres?: string[]
      tags?: string[]
    },
  ) {
    return this.request<Novel>(`/api/novels/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  async deleteNovel(id: string) {
    return this.request<void>(`/api/novels/${id}`, {
      method: "DELETE",
    })
  }

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
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        const stringValue = value.toString().trim()
        if (stringValue.length > 0) {
          searchParams.append(key, stringValue)
        }
      }
    })

    return this.request<PageResponse<Novel>>(`/api/novels/search?${searchParams.toString()}`)
  }

  async getCategories() {
    return this.request<Category[]>("/api/novels/categories")
  }

  async createCategory(name: string, description?: string) {
    var id = crypto.randomUUID();
    return this.request<Category>("/api/novels/categories", {
      method: "POST",
      body: JSON.stringify({id, name, description }),
    })
  }

  async updateCategory(id: string, name: string, description?: string) {
    return this.request<Category>(`/api/novels/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify({ name, description }),
    })
  }
  async getGenres() {
    return this.request<Genre[]>("/api/novels/genres")
  }

  async createGenre(name: string, description?: string) {
    var id = crypto.randomUUID();
    return this.request<Genre>("/api/novels/genres", {
      method: "POST",
      body: JSON.stringify({id, name, description }),
    })
  }

  async updateGenre(id: string, name: string, description?: string) {
    return this.request<Genre>(`/api/novels/genres/${id}`, {
      method: "PUT",
      body: JSON.stringify({ name, description }),
    })
  }

  async getTags() {
    return this.request<Tag[]>("/api/novels/tags")
  }

  async createTag(name: string, description?: string) {
    var id = crypto.randomUUID();
    return this.request<Tag>("/api/novels/tags", {
      method: "POST",
      body: JSON.stringify({id, name, description }),
    })
  }
  async updateTag(id: string, name: string, description?: string) {
    return this.request<Tag>(`/api/novels/tags/${id}`, {
      method: "PUT",
      body: JSON.stringify({ name, description }),
    })
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

  async getRecentlyRead(size = 6) {
    return this.request<Novel[]>(`/api/reading-history`)
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

    return this.request<PageResponse<Chapter>>(`/api/chapters/novel/${novelId}?${searchParams.toString()}`)
  }

  async getChapterById(id: string) {
    return this.request<ChapterDetail>(`/api/chapters/${id}`)
  }

  async getChapterByNumber(novelId: string, chapterNumber: number) {
    return this.request<ChapterDetail>(`/api/chapters/novel/${novelId}/chapter/${chapterNumber}`)
  }

  async getChapterByNumber2(slug: string, chapterNumber: number) {
    return this.request<ChapterDetail>(`/api/chapters/novel/slug/${slug}/chapter/${chapterNumber}`)
  }

  async createChapter(data: {
    title: string
    chapterNumber: number
    contentHtml?: string
    content?: string
    format?: "HTML" | "TEXT"
    novelId: string
  }) {
    return this.request<Chapter>("/api/chapters", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async updateChapter(
    id: string,
    data: {
      title: string
      chapterNumber: number
      // content for TEXT, contentHtml for HTML
      content?: string
      contentHtml?: string
      format?: "HTML" | "TEXT"
      novelId: string
    },
  ) {
    return this.request<Chapter>(`/api/chapters/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  async deleteChapter(id: string) {
    return this.request<void>(`/api/chapters/${id}`, {
      method: "DELETE",
    })
  }

  async getChapterJsonMetadata(chapterId: string) {
    return this.request<FileMetadata>(`/api/chapters/${chapterId}/json-metadata`)
  }

  async getChapterAudioMetadata(chapterId: string) {
    return this.request<FileMetadata>(`/api/chapters/${chapterId}/audio-metadata`)
  }

  async getChapterAudio(chapterId: string) {
    return this.request<ChapterDetail>(`/api/chapters/${chapterId}/audio`)
  }

  async updateReadingProgress(novelId: string, lastReadChapterIndex: number) {
    return this.request<ReadingHistory>(
      `/api/reading-history/novel/${novelId}?lastReadChapterIndex=${lastReadChapterIndex}`,
      {
        method: "POST",
      },
    )
  }

  async updateReadingProgress(novelId: string, chapterId: string, progress: number, readingTime = 0) {
    const params = new URLSearchParams({
      progress: progress.toString(),
      readingTime: readingTime.toString(),
    })
    return this.request<ReadingHistory>(
      `/api/reading-history/novel/${novelId}/chapter/${chapterId}/progress?${params}`,
      { method: "POST" },
    )
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

  async getChapterComments(
    chapterId: string,
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

    return this.request<PageResponse<Comment>>(`/api/comments/chapter/${chapterId}?${searchParams}`)
  }

  async addComment(data: {
    content: string
    novelId?: string
    chapterId?: string
    parentId?: string
  }) {
    return this.request<Comment>("/api/comments", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async updateComment(commentId: string, data: { content: string }) {
    return this.request<Comment>(`/api/comments/${commentId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  async deleteComment(commentId: string) {
    return this.request<void>(`/api/comments/${commentId}`, {
      method: "DELETE",
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
    return this.request<ApiResponse<number>>("/api/notifications/unread/count")
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
      search?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()

    // Set default values
    const page = params.page ?? 0
    const size = params.size ?? 10
    const sortBy = params.sortBy ?? "id"
    const sortDir = params.sortDir ?? "asc"

    // Add pagination parameters
    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    // Add search parameter if provided
    if (params.search && params.search.trim()) {
      searchParams.append("search", params.search.trim())
    }

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

  // Report endpoints
  async createReport(data: {
    reportType: "NOVEL" | "CHAPTER" | "COMMENT" | "REVIEW" | "USER"
    targetId: string
    reason: "SPAM" | "INAPPROPRIATE_CONTENT" | "COPYRIGHT_VIOLATION" | "HARASSMENT" | "MISINFORMATION" | "OTHER"
    description: string
  }) {
    return this.request<Report>("/api/reports", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

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

    // Set default values
    const page = params.page ?? 0
    const size = params.size ?? 20
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    // Add pagination parameters
    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    // Add filter parameters if provided
    if (params.status) {
      searchParams.append("status", params.status)
    }
    if (params.reportType) {
      searchParams.append("reportType", params.reportType)
    }

    return this.request<PageResponse<Report>>(`/api/reports/my-reports?${searchParams}`)
  }

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

    // Set default values
    const page = params.page ?? 0
    const size = params.size ?? 20
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    // Add pagination parameters
    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    // Add filter parameters if provided
    if (params.status) {
      searchParams.append("status", params.status)
    }
    if (params.category) {
      searchParams.append("category", params.category)
    }

    return this.request<PageResponse<FeatureRequest>>(`/api/feature-requests?${searchParams}`)
  }

  async createFeatureRequest(data: {
    title: string
    description: string
    category: string
  }) {
    return this.request<FeatureRequest>("/api/feature-requests", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async voteFeatureRequest(featureRequestId: string) {
    return this.request<FeatureRequest>(`/api/feature-requests/${featureRequestId}/vote`, {
      method: "POST",
    })
  }

  async unvoteFeatureRequest(featureRequestId: string) {
    return this.request<FeatureRequest>(`/api/feature-requests/${featureRequestId}/vote`, {
      method: "DELETE",
    })
  }

  // Role Approval endpoints
  async requestRole(requestedRole: string, reason: string) {
    return this.request<RoleRequest>("/api/role-approval/request", {
      method: "POST",
      body: JSON.stringify({ requestedRole, reason }),
    })
  }

  async getPendingRoleRequests(
    params: {
      page?: number
      size?: number
      sortBy?: string
      sortDir?: string
    } = {},
  ) {
    const searchParams = new URLSearchParams()

    // Set default values
    const page = params.page ?? 0
    const size = params.size ?? 10
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    // Add pagination parameters
    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return this.request<PageResponse<RoleRequest>>(`/api/role-approval/pending?${searchParams}`)
  }

  async getMyRoleRequests(
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

    return this.request<PageResponse<RoleRequest>>(`/api/role-approval/my-requests?${searchParams}`)
  }

  async approveRoleRequest(requestId: string) {
    return this.request<RoleRequest>(`/api/role-approval/approve/${requestId}`, {
      method: "POST",
    })
  }

  async rejectRoleRequest(requestId: string) {
    return this.request<RoleRequest>(`/api/role-approval/reject/${requestId}`, {
      method: "POST",
    })
  }

  // File upload method
  async uploadFile(file: File, type?: string): Promise<ApiResponse<FileMetadata>> {
    const formData = new FormData()
    formData.append("file", file)

    if (type) {
      formData.append("type", type)
    }

    const headers: HeadersInit = {}
    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`
    }

    const response = await fetch(`${this.baseURL}/api/files/upload`, {
      method: "POST",
      headers,
      body: formData,
    })

    if (!response.ok) {
      throw new Error(`Upload failed: ${response.statusText}`)
    }

    return response.json()
  }

  async uploadEpub(file: File): Promise<ApiResponse<FileMetadata>> {
    const formData = new FormData()
    formData.append("file", file)
    formData.append("type", "NOVEL_EPUB")

    const headers: HeadersInit = {}
    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`
    }

    try {
      const response = await fetch(`${this.baseURL}/api/files/upload`, {
        method: "POST",
        headers,
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
  }

  async addChaptersFromEpub(id: string | undefined, epubFile: File): Promise<ApiResponse<FileMetadata>> {
    const formData = new FormData()
    formData.append("epub", epubFile) // required

    const headers: HeadersInit = {}
    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`
    }

    try {
      const response = await fetch(`${this.baseURL}/api/novels/${id}/import-epub`, {
        method: "POST",
        headers,
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
  }


  private baseURL: string
  private token: string | null = null

  constructor(baseURL: string) {
    this.baseURL = baseURL
    if (typeof window !== "undefined") {
      this.token = localStorage.getItem("token")
    }
  }

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

    return this.request<PageResponse<Review>>(`/api/reviews/novel/${novelId}?${searchParams}`)
  }

  async createReview(data: {
    novelId: string
    title: string
    content: string
    rating: number
  }) {
    return this.request<Review>("/api/reviews", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async updateReview(
    reviewId: string,
    data: {
      title: string
      content: string
      rating: number
    },
  ) {
    return this.request<Review>(`/api/reviews/${reviewId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  async deleteReview(reviewId: string) {
    return this.request<void>(`/api/reviews/${reviewId}`, {
      method: "DELETE",
    })
  }

  // File Management endpoints
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

    return this.request<PageResponse<FileMetadata>>(`/api/files?${searchParams}`)
  }

  async deleteFile(fileId: string) {
    return this.request<void>(`/api/files/${fileId}`, {
      method: "DELETE",
    })
  }

  // Notification endpoints for admin
  async getAllNotifications(
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
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return this.request<PageResponse<Notification>>(`/api/notifications?${searchParams}`)
  }

  async createSystemNotification(data: {
    title: string
    message: string
    type: string
  }) {
    return this.request<Notification>("/api/notifications", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async updateNotification(
    notificationId: string,
    data: {
      title: string
      message: string
    },
  ) {
    return this.request<Notification>(`/api/notifications/${notificationId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  async deleteNotification(notificationId: string) {
    return this.request<void>(`/api/notifications/${notificationId}`, {
      method: "DELETE",
    })
  }

  // Comments Management endpoints
  async getAllComments(
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
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return this.request<PageResponse<Comment>>(`/api/comments/admin?${searchParams}`)
  }

  async approveComment(commentId: string) {
    return this.request<Comment>(`/api/comments/${commentId}/approve`, {
      method: "POST",
    })
  }

  async rejectComment(commentId: string) {
    return this.request<Comment>(`/api/comments/${commentId}/reject`, {
      method: "POST",
    })
  }

  // Reviews Management endpoints
  async getAllReviews(
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
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return this.request<PageResponse<Review>>(`/api/reviews?${searchParams}`)
  }

  async flagReview(reviewId: string, reason: string) {
    return this.request<Review>(`/api/reviews/${reviewId}/flag`, {
      method: "POST",
      body: JSON.stringify({ reason }),
    })
  }

  // Reports Management endpoints
  async getAllReports(
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

    return this.request<PageResponse<Report>>(`/api/reports/admin?${searchParams}`)
  }

  async updateReportStatus(reportId: string, status: string) {
    return this.request<Report>(`/api/reports/${reportId}/status`, {
      method: "PUT",
      body: JSON.stringify({ status }),
    })
  }

  async resolveReport(reportId: string, action: string) {
    return this.request<Report>(`/api/reports/${reportId}/resolve`, {
      method: "POST",
      body: JSON.stringify({ action }),
    })
  }

  // Messages Management endpoints
  async getAllMessages(
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
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return this.request<PageResponse<any>>(`/api/messages/admin?${searchParams}`)
  }

  async deleteMessage(messageId: string) {
    return this.request<void>(`/api/messages/${messageId}`, {
      method: "DELETE",
    })
  }

  async getMessagesByGroup(
    groupId: string,
    params: {
      page?: number
      size?: number
    } = {},
  ) {
    const searchParams = new URLSearchParams()
    const page = params.page ?? 0
    const size = params.size ?? 20

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())

    return this.request<PageResponse<any>>(`/api/messages/group/${groupId}?${searchParams}`)
  }

  // Groups Management endpoints
  async getAllGroups(
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
    const sortBy = params.sortBy ?? "createdAt"
    const sortDir = params.sortDir ?? "desc"

    searchParams.append("page", page.toString())
    searchParams.append("size", size.toString())
    searchParams.append("sortBy", sortBy)
    searchParams.append("sortDir", sortDir)

    return this.request<PageResponse<any>>(`/api/groups/me`)
  }

  async deleteGroup(groupId: string) {
    return this.request<void>(`/api/groups/${groupId}`, {
      method: "DELETE",
    })
  }

  async getGroupMembers(groupId: string) {
    return this.request<any[]>(`/api/groups/${groupId}/members`)
  }

  // Message and group API methods
  async sendMessage(groupId: string, content: string) {
    return this.request<any>("/api/messages", {
      method: "POST",
      body: JSON.stringify({ groupId, content }),
    })
  }

  async getUserGroups() {
    return this.request<any[]>("/api/groups/my-groups")
  }

  async createGroup(name: string, description?: string, isPrivate = false) {
    return this.request<any>("/api/groups", {
      method: "POST",
      body: JSON.stringify({ name, description, isPrivate }),
    })
  }

  async joinGroup(groupId: string) {
    return this.request<any>(`/api/groups/${groupId}/join`, {
      method: "POST",
    })
  }

  async leaveGroup(groupId: string) {
    return this.request<any>(`/api/groups/${groupId}/leave`, {
      method: "POST",
    })
  }

  async updateGroup(groupId: string, data: { name?: string; description?: string }) {
    return this.request<any>(`/api/groups/${groupId}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  async addGroupMember(groupId: string, userId: string) {
    return this.request<any>(`/api/groups/${groupId}/members/${userId}`, {
      method: "POST",
    })
  }

  async removeGroupMember(groupId: string, userId: string) {
    return this.request<any>(`/api/groups/${groupId}/members/${userId}`, {
      method: "DELETE",
    })
  }
}

export const api = new ApiClient(API_BASE_URL)
