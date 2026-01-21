import type { ApiResponse, User } from "@/models"
import type { ApiClient } from "../api-client"
import crypto from "crypto"

// Hash password using SHA-256
function hashPassword(password: string): string {
  return crypto.createHash("sha256").update(password).digest("hex")
}

export const createAuthApi = (client: ApiClient) => ({
  async login(email: string, password: string) {
      const hashedPassword = hashPassword(password)
    const response = await client.request<{
      id: string
      username: string
      email: string
      roles: string[]
      tokenType: string
      accessToken: string
    }>("/api/auth/signin", {
      method: "POST",
      body: JSON.stringify({ email, password: hashedPassword }),
    })

    if (response.success && response.data.accessToken) {
      client.setToken(response.data.accessToken)
    }

    return response
  },

  async register(username: string, email: string, password: string): Promise<ApiResponse<string>> {
      const hashedPassword = hashPassword(password)
    return client.request("/api/auth/signup", {
      method: "POST",
      body: JSON.stringify({ username, email, password: hashedPassword }),
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
      const hashedOldPassword = hashPassword(oldPassword)
      const hashedNewPassword = hashPassword(newPassword)
      const hashedConfirmPassword = hashPassword(confirmPassword)
    
    return client.request<void>("/api/users/change-password", {
      method: "PUT",
      body: JSON.stringify({ oldPassword: hashedOldPassword, newPassword: hashedNewPassword, confirmPassword: hashedConfirmPassword }),
    })
  },
})
