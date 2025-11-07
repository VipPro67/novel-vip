import { api, type ApiResponse, type PageResponse, type Novel, type Category } from "./api"

// Extended admin-specific interfaces
export interface AdminStats {
  totalUsers: number
  totalNovels: number
  totalChapters: number
  totalViews: number
  newUsersThisMonth: number
  newNovelsThisMonth: number
  popularNovels: Novel[]
  recentActivity: ActivityLog[]
}

export interface ActivityLog {
  id: string
  userId: string
  username: string
  action: string
  resourceType: string
  resourceId: string
  timestamp: string
  details?: string
}

export interface BulkOperation {
  action: "delete" | "updateStatus" | "updateCategory"
  novelIds: string[]
  newStatus?: string
  newCategoryId?: string
}

class AdminApiClient {
  // System Analytics
  async getSystemStats(): Promise<ApiResponse<AdminStats>> {
    return api.request<AdminStats>("/api/admin/stats")
  }

  async getActivityLogs(
    params: {
      page?: number
      size?: number
      userId?: string
      action?: string
      startDate?: string
      endDate?: string
    } = {},
  ): Promise<ApiResponse<PageResponse<ActivityLog>>> {
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value.toString())
      }
    })

    return api.request<PageResponse<ActivityLog>>(`/api/admin/activity-logs?${searchParams}`)
  }

  // Bulk Operations
  async bulkUpdateNovels(operation: BulkOperation): Promise<ApiResponse<void>> {
    return api.request<void>("/api/admin/novels/bulk", {
      method: "POST",
      body: JSON.stringify(operation),
    })
  }

  async bulkDeleteNovels(novelIds: string[]): Promise<ApiResponse<void>> {
    return api.request<void>("/api/admin/novels/bulk-delete", {
      method: "DELETE",
      body: JSON.stringify({ novelIds }),
    })
  }

  // Category Management
  async getCategories(
    params: {
      page?: number
      size?: number
      search?: string
    } = {},
  ): Promise<ApiResponse<PageResponse<Category>>> {
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value.toString())
      }
    })

    return api.request<PageResponse<Category>>(`/api/admin/categories?${searchParams}`)
  }

  async createCategory(data: {
    name: string
    slug: string
    description?: string
  }): Promise<ApiResponse<Category>> {
    return api.request<Category>("/api/admin/categories", {
      method: "POST",
      body: JSON.stringify(data),
    })
  }

  async updateCategory(
    id: string,
    data: {
      name: string
      slug: string
      description?: string
    },
  ): Promise<ApiResponse<Category>> {
    return api.request<Category>(`/api/admin/categories/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    })
  }

  async deleteCategory(id: string): Promise<ApiResponse<void>> {
    return api.request<void>(`/api/admin/categories/${id}`, {
      method: "DELETE",
    })
  }

  // File Management
  async getUploadedFiles(
    params: {
      page?: number
      size?: number
      type?: string
      search?: string
    } = {},
  ): Promise<ApiResponse<PageResponse<any>>> {
    const searchParams = new URLSearchParams()
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value.toString())
      }
    })

    return api.request<PageResponse<any>>(`/api/admin/files?${searchParams}`)
  }

  async deleteFile(fileId: string): Promise<ApiResponse<void>> {
    return api.request<void>(`/api/admin/files/${fileId}`, {
      method: "DELETE",
    })
  }

  // System Backup
  async createBackup(): Promise<ApiResponse<{ backupId: string; downloadUrl: string }>> {
    return api.request<{ backupId: string; downloadUrl: string }>("/api/admin/backup", {
      method: "POST",
    })
  }

  async getBackupHistory(): Promise<ApiResponse<any[]>> {
    return api.request<any[]>("/api/admin/backup/history")
  }

  async restoreBackup(backupId: string): Promise<ApiResponse<void>> {
    return api.request<void>(`/api/admin/backup/${backupId}/restore`, {
      method: "POST",
    })
  }

  // Export Data
  async exportNovels(format: "csv" | "json" = "csv"): Promise<ApiResponse<{ downloadUrl: string }>> {
    return api.request<{ downloadUrl: string }>(`/api/admin/export/novels?format=${format}`, {
      method: "POST",
    })
  }

  async exportUsers(format: "csv" | "json" = "csv"): Promise<ApiResponse<{ downloadUrl: string }>> {
    return api.request<{ downloadUrl: string }>(`/api/admin/export/users?format=${format}`, {
      method: "POST",
    })
  }

  // System Settings
  async getSystemSettings(): Promise<ApiResponse<any>> {
    return api.request<any>("/api/admin/settings")
  }

  async updateSystemSettings(settings: any): Promise<ApiResponse<any>> {
    return api.request<any>("/api/admin/settings", {
      method: "PUT",
      body: JSON.stringify(settings),
    })
  }
}

export const adminApi = new AdminApiClient()
