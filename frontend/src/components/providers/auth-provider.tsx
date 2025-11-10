"use client"

import { ApiResponse, User } from "@/models"
import { api } from "@/services/api"
import type { ApiError } from "@/services/api-client"
import type React from "react"
import { createContext, useContext, useEffect, useState } from "react"

interface AuthContextType {
  user: User | null
  login: (email: string, password: string) => Promise<AuthResponse>
  loginWithGoogle: (credential: string) => Promise<AuthResponse>
  register: (username: string, email: string, password: string) => Promise<ApiResponse<string>>
  logout: () => void
  loading: boolean
  isAuthenticated: boolean
  hasRole: (role: string) => boolean
}
type AuthResponse = ApiResponse<{
  accessToken?: string
  id?: string
  username?: string
  email?: string
  roles?: string[]
}>

const AuthContext = createContext<AuthContextType | undefined>(undefined)

// Helper function to decode JWT token
function decodeJWT(token: string) {
  try {
    const base64Url = token.split(".")[1]
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/")
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join(""),
    )
    return JSON.parse(jsonPayload)
  } catch (error) {
    console.error("Error decoding JWT:", error)
    return null
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem("token")
    if (token) {
      api.setToken(token)
      const decodedToken = decodeJWT(token)
      if (decodedToken) {
        const currentTime = Date.now() / 1000
        if (decodedToken.exp < currentTime) {
          api.clearToken()
          setLoading(false)
          return
        }

        const userData: User = {
          id: decodedToken.userId,
          username: decodedToken.sub,
          email: decodedToken.email,
          roles: decodedToken.roles || [],
        }
        setUser(userData)

        fetchUserProfile()
      } else {
        api.clearToken()
        setLoading(false)
      }
    } else {
      setLoading(false)
    }
  }, [])

  const buildUserFromAuthData = (payload?: AuthResponse["data"]): User | null => {
    if (!payload?.accessToken && (!payload?.id || !payload?.username || !payload?.email)) {
      return null
    }

    const decodedToken = payload?.accessToken ? decodeJWT(payload.accessToken) : null
    const id = decodedToken?.userId || payload?.id
    const username = decodedToken?.sub || payload?.username
    const email = decodedToken?.email || payload?.email

    if (!id || !username || !email) {
      return null
    }

    return {
      id: String(id),
      username: String(username),
      email: String(email),
      roles: decodedToken?.roles || payload?.roles || [],
    }
  }

  const applyAuthResponse = (response?: AuthResponse) => {
    if (!response?.success || !response.data) {
      return
    }

    const userData = buildUserFromAuthData(response.data)
    if (userData) {
      setUser(userData)
    }
  }

  const fetchUserProfile = async () => {
    try {
      const response = await api.getUserProfile()
      if (response.success) {
        setUser((prevUser) => ({
          ...response.data,
          roles: prevUser?.roles || response.data.roles || [],
        }))
      }
    } catch (error) {
      console.error("Failed to fetch user profile:", error)
    } finally {
      setLoading(false)
    }
  }

  const login = async (email: string, password: string): Promise<AuthResponse> => {
    try {
      const response = await api.login(email, password)
      applyAuthResponse(response)
      return response
    } catch (error) {
      console.error("Login failed:", error)
      const apiError = error as ApiError | Error
      return {
        success: false,
        message: apiError instanceof Error ? apiError.message : "An error occurred",
        data: undefined,
        statusCode: (apiError as ApiError)?.status ?? 500,
      } as AuthResponse
    }
  }

  const loginWithGoogle = async (credential: string): Promise<AuthResponse> => {
    try {
      const response = await api.loginWithGoogle(credential)
      applyAuthResponse(response)
      return response
    } catch (error) {
      console.error("Google login failed:", error)
      const apiError = error as ApiError | Error
      return {
        success: false,
        message: apiError instanceof Error ? apiError.message : "Unable to authenticate with Google",
        data: undefined,
        statusCode: (apiError as ApiError)?.status ?? 500,
      } as AuthResponse
    }
  }

  const register = async (username: string, email: string, password: string): Promise<ApiResponse<string>> => {
    const response = await api.register(username, email, password)
    return response
  }

  const logout = () => {
    api.clearToken()
    if (typeof window !== "undefined") {
      window.localStorage.removeItem("readerSettings")
    }
    setUser(null)
  }

  const hasRole = (role: string): boolean => {
    if (!user) {
      return false
    }
    for (const r of user.roles) {
      if (r === role) {
        return true
      }
    }
    return false
  }

  const value: AuthContextType = {
    user,
    login,
    loginWithGoogle,
    register,
    logout,
    loading,
    isAuthenticated: !!user,
    hasRole,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
