"use client"

import type React from "react"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/components/providers/auth-provider"

interface AuthGuardProps {
  children: React.ReactNode
  requireAdmin?: boolean
}

export function AuthGuard({ children, requireAdmin = false }: AuthGuardProps) {
  const { isAuthenticated, hasRole, loading, user } = useAuth()
  const router = useRouter()

  useEffect(() => {
    if (!loading) {
      console.log("AuthGuard check:", {
        isAuthenticated,
        user,
        requireAdmin,
        hasAdminRole: hasRole("ADMIN"),
        userRoles: user?.roles,
      })

      if (!isAuthenticated) {
        console.log("Not authenticated, redirecting to login")
        router.push("/login")
        return
      }

      if (requireAdmin && !hasRole("ADMIN")) {
        console.log("Admin required but user does not have ADMIN role")
        router.push("/")
        return
      }
    }
  }, [isAuthenticated, hasRole, loading, requireAdmin, router, user])

  // Show loading while checking authentication
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary"></div>
      </div>
    )
  }

  // Don't render children if not authenticated
  if (!isAuthenticated) {
    return null
  }

  // Don't render children if admin required but user is not admin
  if (requireAdmin && !hasRole("ADMIN")) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold mb-2">Access Denied</h1>
          <p className="text-muted-foreground">You don't have permission to access this page.</p>
          <p className="text-sm text-muted-foreground mt-2">
            Required: ADMIN role | Your roles: {user?.roles.join(", ") || "None"}
          </p>
        </div>
      </div>
    )
  }

  return <>{children}</>
}
