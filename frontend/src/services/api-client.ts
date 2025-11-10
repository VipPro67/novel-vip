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

  async request<T>(endpoint: string, options: RequestInit = {}) {
    const url = `${this.baseURL}${endpoint}`
    const headers: HeadersInit = { ...options.headers }
    const isFormData = options.body instanceof FormData

    if (!isFormData) {
      headers["Content-Type"] = "application/json"
    }

    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`
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
        let fallbackMessage = `Request failed with status ${response.status}`
        if (response.status === 401) {
          this.clearToken()
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
}

export const createApiClient = (baseURL: string = API_BASE_URL) => new ApiClient(baseURL)
