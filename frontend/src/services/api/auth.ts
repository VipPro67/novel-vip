import type { ApiResponse, User } from "@/models"
import type { ApiClient } from "../api-client"

export const createAuthApi = (client: ApiClient) => ({
  async login(email: string, password: string) {
    const response = await client.request<{
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
      client.setToken(response.data.accessToken)
    }

    return response
  },

  async register(username: string, email: string, password: string): Promise<ApiResponse<string>> {
    return client.request("/api/auth/signup", {
      method: "POST",
      body: JSON.stringify({ username, email, password }),
    })
  },

  async verifyEmail(token: string): Promise<ApiResponse<string>> {
    return client.request(`/api/auth/verify-email?token=${encodeURIComponent(token)}`)
  },

  async loginWithGoogle(credential: string) {
    const response = await client.request<{
      id: string
      username: string
      email: string
      roles: string[]
      tokenType: string
      accessToken: string
    }>("/api/auth/google", {
      method: "POST",
      body: JSON.stringify({ credential }),
    })

    if (response.success && response.data.accessToken) {
      client.setToken(response.data.accessToken)
    }

    return response
  },

  async verifyGoogleAccount(credential: string) {
    return client.request<{ email: string | null; emailVerified: boolean; fullName?: string; avatar?: string }>(
      "/api/auth/google/verify",
      {
        method: "POST",
        body: JSON.stringify({ credential }),
      },
    )
  },

  async getUserProfile() {
    return client.request<User>("/api/users/profile")
  },

  async updateUserProfile(data: { fullName?: string; avatar?: string }) {
    return client.request<User>("/api/users/profile", {
      method: "PUT",
      body: JSON.stringify(data),
    })
  },

  async changePassword(oldPassword: string, newPassword: string, confirmPassword: string) {
    return client.request<void>("/api/users/change-password", {
      method: "PUT",
      body: JSON.stringify({ oldPassword, newPassword, confirmPassword }),
    })
  },
})
