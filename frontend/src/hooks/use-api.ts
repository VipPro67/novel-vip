"use client"

import { useEffect, useMemo } from "react"
import { useAuth } from "@/components/providers/auth-provider"
import { api } from "@/services/api"

// Cache localStorage reads to avoid repeated access
let cachedToken: string | null = null
let tokenCacheTime = 0
const TOKEN_CACHE_DURATION = 1000 // 1 second

function getCachedToken(): string | null {
  if (typeof window === "undefined") return null
  
  const now = Date.now()
  if (cachedToken !== null && now - tokenCacheTime < TOKEN_CACHE_DURATION) {
    return cachedToken
  }
  
  cachedToken = localStorage.getItem("token")
  tokenCacheTime = now
  return cachedToken
}

export function useApi() {
  const { isAuthenticated } = useAuth()

  useEffect(() => {
    const token = getCachedToken()
    if (token && isAuthenticated) {
      api.setToken(token)
    } else if (!isAuthenticated) {
      api.clearToken()
      cachedToken = null
    }
  }, [isAuthenticated])

  return useMemo(() => api, [])
}
