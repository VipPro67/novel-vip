"use client"

import { useEffect } from "react"
import { useAuth } from "@/components/providers/auth-provider"
import { api } from "@/lib/api"

export function useApi() {
  const { user, isAuthenticated } = useAuth()

  useEffect(() => {
    // Ensure API client has the current token
    const token = localStorage.getItem("token")
    if (token && isAuthenticated) {
      api.setToken(token)
    } else if (!isAuthenticated) {
      api.clearToken()
    }
  }, [isAuthenticated, user])

  return api
}
