import { NextResponse } from "next/server"
import type { NextRequest } from "next/server"

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl

  // Protected routes that require authentication
  const protectedRoutes = ["/profile", "/favorites", "/reading-history", "/bookmarks", "/settings", "/notifications"]

  // Admin routes that require admin role
  const adminRoutes = ["/admin"]

  // Check if the current path is protected
  const isProtectedRoute = protectedRoutes.some((route) => pathname.startsWith(route))
  const isAdminRoute = adminRoutes.some((route) => pathname.startsWith(route))

  // For client-side routes that need authentication, we'll handle the redirect in the component
  // since localStorage is not available in middleware
  if (isProtectedRoute || isAdminRoute) {
    // Let the request pass through - authentication will be handled client-side
    return NextResponse.next()
  }

  return NextResponse.next()
}

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|favicon.ico).*)"],
}
