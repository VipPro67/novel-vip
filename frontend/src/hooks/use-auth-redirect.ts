"use client"

import { useRouter, usePathname } from "next/navigation"
import { useAuth } from "@/components/providers/auth-provider"
import { useEffect } from "react"

export function useAuthRedirect() {
  const router = useRouter()
  const pathname = usePathname()
  const { isAuthenticated, loading, hasRole } = useAuth()

  const redirectToLogin = () => {
    // Don't redirect if already on login/register pages
    if (pathname === "/login" || pathname === "/register") {
      return
    }

    // Encode the current path as redirect parameter
    const redirectUrl = encodeURIComponent(pathname)
    router.push(`/login?redirect=${redirectUrl}`)
  }

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      redirectToLogin()
    }
  }, [loading, isAuthenticated])

  const requireRole = (role: string) => {
    useEffect(() => {
      if (!loading) {
        if (!isAuthenticated) {
          redirectToLogin()
        } else if (!hasRole(role)) {
          // Redirect to unauthorized page or home
          router.push("/")
        }
      }
    }, [loading, isAuthenticated, hasRole])
  }

  return {
    redirectToLogin,
    requireRole,
  }
}
