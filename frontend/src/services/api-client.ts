import type { ApiResponse } from "@/models"

export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8081"

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

      if (!response.ok) {
        if (response.status === 401) {
          this.clearToken()
          
          throw new Error("Authentication failed. Please login again.")
        }
        if (response.status === 403) {
          throw new Error("Access denied. Insufficient permissions.")
        }
        if (response.status === 404) {
          throw new Error("Resource not found.")
        }
        if (response.status >= 500) {
          throw new Error("Server error. Please try again later.")
        }

        throw new Error(`HTTP error! status: ${response.status}`)
      }

      return (await response.json()) as ApiResponse<T>
    } catch (error) {
      console.error("API Request Error:", error)
      throw error
    }
  }
}

export const createApiClient = (baseURL: string = API_BASE_URL) => new ApiClient(baseURL)
