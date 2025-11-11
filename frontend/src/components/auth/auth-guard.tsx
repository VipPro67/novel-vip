"use client"

import type React from "react"

import { useEffect } from "react"
import { usePathname, useRouter } from "@/navigation"
import { useAuth } from "@/components/providers/auth-provider"
import { Skeleton } from "@/components/ui/skeleton"

interface AuthGuardProps {
  children: React.ReactNode
  requireRole?: string[]
  fallback?: React.ReactNode
}

export function AuthGuard({ children, requireRole, fallback }: AuthGuardProps) {
  const { user, loading, isAuthenticated, hasRole } = useAuth()
  const router = useRouter()
  const pathname = usePathname()

  useEffect(() => {
    if (!loading) {
      if (!isAuthenticated) {
        router.push(`/`)
        return
      }

      if (requireRole && !hasRole(requireRole)) {
        router.push("/")
        return
      }
    }
  }, [loading, isAuthenticated, hasRole, requireRole, router, pathname])

  if (loading) {
    return (
      fallback || (
        <div className="container mx-auto px-4 py-8">
          <div className="space-y-4">
            <Skeleton className="h-8 w-64" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-full" />
            <Skeleton className="h-4 w-3/4" />
          </div>
        </div>
      )
    )
  }

  if (!isAuthenticated) {
    return null // Will redirect in useEffect
  }

  if (requireRole && !hasRole(requireRole)) {
    return null // Will redirect in useEffect
  }

  return <>{children}</>
}
