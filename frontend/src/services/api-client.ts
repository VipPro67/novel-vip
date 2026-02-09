import type { ApiResponse } from "@/models"

export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081"

export type ApiError = Error & {
  status?: number
  body?: ApiResponse<unknown> | null
}

export class ApiClient {
  protected token: string | null = null

  constructor(public readonly baseURL: string) {
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

  getToken() {
    return this.token
  }

  private isRefreshing = false
  private failedRequestsQueue: Array<{
    resolve: (value: unknown) => void
    reject: (reason?: unknown) => void
  }> = []

  async request<T>(endpoint: string, options: RequestInit = {}): Promise<ApiResponse<T>> {
    const url = `${this.baseURL}${endpoint}`
    const headers: Record<string, string> = { ...(options.headers as Record<string, string> || {}) }
    const isFormData = options.body instanceof FormData

    if (!isFormData) {
      headers["Content-Type"] = "application/json"
    }

    if (this.token) {
      headers["Authorization"] = `Bearer ${this.token}`
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      })

      const contentType = response.headers.get("content-type") || ""
      const responseText = await response.text()
      let parsedBody: ApiResponse<T> | null = null

      if (responseText && contentType.includes("application/json")) {
        try {
          parsedBody = JSON.parse(responseText) as ApiResponse<T>
        } catch (parseError) {
          console.warn("Failed to parse JSON response", parseError)
        }
      }

      if (!response.ok) {
        if (response.status === 401 && !endpoint.includes("/auth/signin")) {
          if (!this.isRefreshing) {
            this.isRefreshing = true
            try {
              const refreshResponse = await this.refreshToken()
              if (refreshResponse.success && refreshResponse.data?.accessToken) {
                this.setToken(refreshResponse.data.accessToken)
                this.processQueue(null, refreshResponse.data.accessToken)

                // Retry original request with new token
                return this.request<T>(endpoint, options)
              } else {
                throw new Error("Refresh failed")
              }
            } catch (refreshError) {
              this.processQueue(refreshError, null)
              this.clearToken()
              // Redirect to login or handle logout?
              // For now just throw the original 401 or refresh error
              const fallbackMessage = "Authentication failed. Please login again."
              const message = parsedBody?.message || fallbackMessage
              const error: ApiError = Object.assign(new Error(message), {
                status: response.status,
                body: parsedBody as ApiResponse<unknown> | null,
              })
              throw error
            } finally {
              this.isRefreshing = false
            }
          } else {
            // Add to queue
            return new Promise((resolve, reject) => {
              this.failedRequestsQueue.push({
                resolve: (token: any) => {
                  // Retry with new token
                  const newHeaders = { ...headers, Authorization: `Bearer ${token}` }
                  resolve(this.request<T>(endpoint, { ...options, headers: newHeaders }))
                },
                reject: (err) => {
                  reject(err)
                }
              })
            })
          }
        }

        let fallbackMessage = `Request failed with status ${response.status}`
        if (response.status === 401) {
          fallbackMessage = "Authentication failed. Please login again."
        } else if (response.status === 403) {
          fallbackMessage = "Access denied. Insufficient permissions."
        } else if (response.status === 404) {
          fallbackMessage = "Resource not found."
        } else if (response.status >= 500) {
          fallbackMessage = "Server error. Please try again later."
        }

        const message = parsedBody?.message || fallbackMessage
        const error: ApiError = Object.assign(new Error(message), {
          status: response.status,
          body: parsedBody as ApiResponse<unknown> | null,
        })
        throw error
      }

      if (parsedBody) {
        return parsedBody
      }

      return {
        success: true,
        message: "",
        data: undefined as T,
        statusCode: response.status,
      }
    } catch (error) {
      console.error("API Request Error:", error)
      throw error
    }
  }

  private async refreshToken(): Promise<ApiResponse<{ accessToken: string } | undefined>> {
    try {
      // The refresh token is in HttpOnly cookie, so we don't send it manually in body/header
      // unless backend expects it differently. The implementation assumes cookie.
      // But we pass credentials: 'include' to send cookies.

      const response = await fetch(`${this.baseURL}/api/auth/refresh`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        credentials: "include" // Important for sending cookies
      })

      const data = await response.json()
      return data
    } catch (error) {
      console.error("Error refreshing token:", error)
      return {
        success: false,
        message: "Failed to refresh token",
        statusCode: 401,
        data: undefined
      }
    }
  }

  private processQueue(error: any, token: string | null = null) {
    this.failedRequestsQueue.forEach((prom) => {
      if (error) {
        prom.reject(error)
      } else {
        prom.resolve(token)
      }
    })
    this.failedRequestsQueue = []
  }

  async get<T>(endpoint: string, options: RequestInit = {}) {
    return this.request<T>(endpoint, { ...options, method: "GET" })
  }

  async post<T>(endpoint: string, data?: unknown, options: RequestInit = {}) {
    return this.request<T>(endpoint, {
      ...options,
      method: "POST",
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async put<T>(endpoint: string, data?: unknown, options: RequestInit = {}) {
    return this.request<T>(endpoint, {
      ...options,
      method: "PUT",
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async patch<T>(endpoint: string, data?: unknown, options: RequestInit = {}) {
    return this.request<T>(endpoint, {
      ...options,
      method: "PATCH",
      body: data ? JSON.stringify(data) : undefined,
    })
  }

  async delete<T>(endpoint: string, options: RequestInit = {}) {
    return this.request<T>(endpoint, { ...options, method: "DELETE" })
  }
}

export const createApiClient = (baseURL: string = API_BASE_URL) => new ApiClient(baseURL)
