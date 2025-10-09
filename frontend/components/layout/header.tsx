"use client"

import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import {
  BookOpen,
  Search,
  User,
  LogOut,
  Settings,
  Shield,
  Menu,
  BookmarkIcon,
  Clock,
  Lightbulb,
  Flag,
} from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { NotificationBell } from "../ui/notification-bell"
import { useAuth } from "@/components/providers/auth-provider"
import { ThemeToggle } from "@/components/ui/theme-toggle"
import { useState } from "react"
import { Sheet, SheetContent, SheetTrigger } from "@/components/ui/sheet"
import SearchBar from "../ui/search-bar"

export function Header() {
  const { user, logout, isAuthenticated, hasRole } = useAuth()
  const pathname = usePathname()
  const router = useRouter()
  const [isOpen, setIsOpen] = useState(false)

  const handleProtectedNavigation = (path: string) => {
    if (!isAuthenticated) {
      // Redirect to login with current page as redirect
      const redirectUrl = encodeURIComponent(path)
      router.push(`/login?redirect=${redirectUrl}`)
    } else {
      router.push(path)
    }
  }

  const handleLogout = () => {
    logout()
    router.push("/")
  }

  const navigation = [
    { name: "Home", href: "/" },
    { name: "Hot", href: "/novels/hot" },
    { name: "Top", href: "/novels/top" },
    { name: "Latest", href: "/novels/latest" },
  ]

  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <Link href="/" className="flex items-center space-x-2">
            <BookOpen className="h-8 w-8 text-primary" />
            <span className="hidden sm:inline text-xl font-bold">Novel VIP Pro</span>
          </Link>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center space-x-6">
            {navigation.map((item) => (
              <Link
                key={item.name}
                href={item.href}
                className={`text-sm font-medium transition-colors hover:text-primary ${
                  pathname === item.href ? "text-primary" : "text-muted-foreground"
                }`}
              >
                {item.name}
              </Link>
            ))}
          </nav>

          {/* Search Bar */}
          <SearchBar />

          {/* Right Side */}
          <div className="flex items-center space-x-4">
            {/* Mobile Search */}
            <Button variant="ghost" size="icon" className="lg:hidden" onClick={() => router.push("/search")}>
              <Search className="h-5 w-5" />
            </Button>

            <ThemeToggle />
            {isAuthenticated && <NotificationBell />}

            {isAuthenticated ? (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                    <Avatar className="h-8 w-8">
                      <AvatarImage src="/placeholder-user.jpg" alt={user?.username} />
                      <AvatarFallback>{user?.username?.charAt(0).toUpperCase()}</AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56" align="end" forceMount>
                  <div className="flex items-center justify-start gap-2 p-2">
                    <div className="flex flex-col space-y-1 leading-none">
                      <p className="font-medium">{user?.username}</p>
                      <p className="w-[200px] truncate text-sm text-muted-foreground">{user?.email}</p>
                    </div>
                  </div>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/profile")}>
                    <User className="mr-2 h-4 w-4" />
                    Profile
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/following")}>
                    <BookmarkIcon className="mr-2 h-4 w-4" />
                    Following Novels
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/history")}>
                    <Clock className="mr-2 h-4 w-4" />
                    Reading History
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/feature-requests")}>
                    <Lightbulb className="mr-2 h-4 w-4" />
                    Feature Requests
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/my-reports")}>
                    <Flag className="mr-2 h-4 w-4" />
                    My Reports
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/settings")}>
                    <Settings className="mr-2 h-4 w-4" />
                    Settings
                  </DropdownMenuItem>
                  {hasRole("admin") && (
                    <DropdownMenuItem onClick={() => router.push("/admin")}>
                      <Shield className="mr-2 h-4 w-4" />
                      Admin Panel
                    </DropdownMenuItem>
                  )}
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout}>
                    <LogOut className="mr-2 h-4 w-4" />
                    Log out
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <div className="flex items-center space-x-2">
                <Button variant="ghost" asChild>
                  <Link href="/login">Sign In</Link>
                </Button>
                <Button asChild>
                  <Link href="/register">Sign Up</Link>
                </Button>
              </div>
            )}

            {/* Mobile Menu */}
            <Sheet open={isOpen} onOpenChange={setIsOpen}>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon" className="md:hidden">
                  <Menu className="h-5 w-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="right" className="w-[300px] sm:w-[400px]">
                <nav className="flex flex-col space-y-4">
                  {navigation.map((item) => (
                    <Link
                      key={item.name}
                      href={item.href}
                      className={`text-sm font-medium transition-colors hover:text-primary ${
                        pathname === item.href ? "text-primary" : "text-muted-foreground"
                      }`}
                      onClick={() => setIsOpen(false)}
                    >
                      {item.name}
                    </Link>
                  ))}
                  <div className="pt-4 border-t">
                    <div className="relative">
                      <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                      <Input
                        placeholder="Search novels..."
                        className="pl-10"
                        onKeyDown={(e) => {
                          if (e.key === "Enter") {
                            const query = (e.target as HTMLInputElement).value
                            if (query.trim()) {
                              router.push(`/search?q=${encodeURIComponent(query)}`)
                              setIsOpen(false)
                            }
                          }
                        }}
                      />
                    </div>
                  </div>
                </nav>
              </SheetContent>
            </Sheet>
          </div>
        </div>
      </div>
    </header>
  )
}
