"use client"

import type React from "react"
import { createContext, useContext, useEffect, useState } from "react"
import { api, ApiResponse, type User } from "@/lib/api"

interface AuthContextType {
  user: User | null
  login: (email: string, password: string) => Promise<AuthResponse>
  register: (username: string, email: string, password: string) => Promise<boolean>
  logout: () => void
  loading: boolean
  isAuthenticated: boolean
  hasRole: (role: string) => boolean
}
interface AuthResponse {
  success: boolean
  message?: string
  data?: {
    accessToken?: string
    id?: string
    username?: string
    email?: string
    roles?: string[]
  }
}

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
    console.log("AuthProvider init - Token from localStorage:", token ? "exists" : "none")

    if (token) {
      // Set token in API client immediately
      api.setToken(token)
      console.log("Token set in API client")

      // Decode token to get user info
      const decodedToken = decodeJWT(token)
      console.log("Decoded token:", decodedToken)

      if (decodedToken) {
        // Check if token is expired
        const currentTime = Date.now() / 1000
        if (decodedToken.exp < currentTime) {
          console.log("Token expired, clearing...")
          // Token expired, clear it
          api.clearToken()
          setLoading(false)
          return
        }

        // Create user object from token
        const userData: User = {
          id: decodedToken.userId,
          username: decodedToken.sub,
          email: decodedToken.email,
          roles: decodedToken.roles || [],
        }
        console.log("User data from token:", userData)
        setUser(userData)

        // Optionally fetch full profile from API
        fetchUserProfile()
      } else {
        console.log("Failed to decode token, clearing...")
        api.clearToken()
        setLoading(false)
      }
    } else {
      setLoading(false)
    }
  }, [])

  const fetchUserProfile = async () => {
    try {
      console.log("Fetching user profile from API...")
      const response = await api.getUserProfile()
      console.log("User profile response:", response)

      if (response.success) {
        // Merge API data with token data, keeping roles from token
        setUser((prevUser) => ({
          ...response.data,
          roles: prevUser?.roles || response.data.roles || [],
        }))
      }
    } catch (error) {
      console.error("Failed to fetch user profile:", error)
      // Don't clear token here, we already have user data from JWT
    } finally {
      setLoading(false)
    }
  }

  const login = async (email: string, password: string): Promise<AuthResponse> => {
    try {
      console.log("Attempting login...")
      const response = await api.login(email, password)
      console.log("Login response:", response)

      if (response.success && response.data.accessToken) {
        // Token is already set in api.login(), but let's decode it
        const decodedToken = decodeJWT(response.data.accessToken)
        console.log("Login - decoded token:", decodedToken)

        if (decodedToken) {
          const userData: User = {
            id: decodedToken.userId || response.data.id,
            username: decodedToken.sub || response.data.username,
            email: decodedToken.email || response.data.email,
            roles: decodedToken.roles || response.data.roles || [],
          }
          console.log("Login - user data:", userData)
          setUser(userData)
          return response
        }
      }
      return response
    } catch (error) {
      console.error("Login failed:", error)
      return {
        success: false,
        message: "An error occurred",
      }
    }
  }

  const register = async (username: string, email: string, password: string): Promise<boolean> => {
    try {
      const response = await api.register(username, email, password)
      return response.success
    } catch (error) {
      console.error("Registration failed:", error)
      return false
    }
  }

  const logout = () => {
    console.log("Logging out...")
    api.clearToken()
    if (typeof window !== "undefined") {
      window.localStorage.removeItem("readerSettings")
    }
    setUser(null)
  }

  const hasRole = (role: string): boolean => {
    const result = user?.roles.includes(role) || false
    console.log(`hasRole(${role}):`, result, "User roles:", user?.roles)
    return result
  }

  const value: AuthContextType = {
    user,
    login,
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
