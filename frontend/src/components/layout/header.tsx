"use client"

import { useEffect, useState } from "react"
import { useTranslations } from "next-intl"
import { Link, usePathname, useRouter } from "@/navigation"
import { BookOpen, Search, User, LogOut, Settings, Shield, BookmarkIcon, Clock, Lightbulb, Flag } from "lucide-react"
import { Button } from "@/components/ui/button"
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
import SearchBar from "../ui/search-bar"
import { useAuthModals } from "@/hooks/use-auth-modals"
import { LocaleSwitcher } from "@/components/locale-switcher"

export function Header() {
  const t = useTranslations("Header")
  const { user, logout, isAuthenticated, hasRole } = useAuth()
  const pathname = usePathname()
  const router = useRouter()
  const [isOpen, setIsOpen] = useState(false)
  const { openLogin, openRegister } = useAuthModals()
  const [hidden, setHidden] = useState(false)
  const [lastScroll, setLastScroll] = useState(0)

  useEffect(() => {
    const handleScroll = () => {
      const current = window.scrollY
      if (current > lastScroll && current > 80) setHidden(true)
      else setHidden(false)
      setLastScroll(current)
    }
    window.addEventListener("scroll", handleScroll)
    return () => window.removeEventListener("scroll", handleScroll)
  }, [lastScroll])

  const handleProtectedNavigation = (path: string) => {
    if (!isAuthenticated) {
      openLogin()
    } else {
      router.push(path)
    }
  }

  const handleLogout = () => {
    logout()
    router.push("/")
  }

  const navigation = [
    { name: t("nav.home"), href: "/" },
    { name: t("nav.hot"), href: "/novels/hot" },
    { name: t("nav.top"), href: "/novels/top" },
    { name: t("nav.latest"), href: "/novels/latest" },
    { name: t("nav.videos"), href: "/videos" },
  ]

  return (
    <header  className={`sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur transition-transform duration-300 ${
        hidden ? "-translate-y-full" : "translate-y-0"
      }`}>
      <div className="container mx-auto px-4">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <Link href="/" className="flex items-center space-x-2">
            <BookOpen className="h-8 w-8 text-primary" />
            <span className="hidden sm:inline text-xl font-bold">{t("brand")}</span>
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
                    {t("actions.profile")}
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/following")}>
                    <BookmarkIcon className="mr-2 h-4 w-4" />
                    {t("actions.following")}
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/history")}>
                    <Clock className="mr-2 h-4 w-4" />
                    {t("actions.history")}
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/feature-requests")}>
                    <Lightbulb className="mr-2 h-4 w-4" />
                    {t("actions.featureRequests")}
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/my-reports")}>
                    <Flag className="mr-2 h-4 w-4" />
                    {t("actions.reports")}
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={() => handleProtectedNavigation("/settings")}>
                    <Settings className="mr-2 h-4 w-4" />
                    {t("actions.settings")}
                  </DropdownMenuItem>
                  {hasRole("admin") && (
                    <DropdownMenuItem onClick={() => router.push("/admin")}>
                      <Shield className="mr-2 h-4 w-4" />
                      {t("actions.admin")}
                    </DropdownMenuItem>
                  )}
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout}>
                    <LogOut className="mr-2 h-4 w-4" />
                    {t("actions.logout")}
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            ) : (
              <div className="flex items-center space-x-2">
                <Button variant="ghost" onClick={openLogin}>
                  {t("actions.signIn")}
                </Button>
                <Button onClick={openRegister}>{t("actions.signUp")}</Button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}
