"use client"

import { useState, useEffect } from "react"
import { Users, BookOpen, FileText, TrendingUp, Eye, Star, MessageCircle, Bell, AlertCircle, PlayCircle } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { AuthGuard } from "@/components/auth/auth-guard"
import { useAuth } from "@/components/providers/auth-provider"
import { api } from "@/services/api"
import Link from "next/link"
import { Button } from "@/components/ui/button"

export default function AdminDashboard() {
  const { user, hasRole } = useAuth()
  return (
    <AuthGuard requireRole="ADMIN">
      <AdminDashboardContent />
    </AuthGuard>
  )
}

function AdminDashboardContent() {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalNovels: 0,
    totalChapters: 0,
    totalViews: 0,
    activeUsers: 0,
    newUsersToday: 0,
    popularNovels: 0,
    avgRating: 0,
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    //fetchDashboardStats()
  }, [])

  const fetchDashboardStats = async () => {
    try {
      // In a real app, you'd have dedicated admin endpoints for these stats
      // For now, we'll simulate with existing endpoints
      const [usersResponse, novelsResponse] = await Promise.all([
        api.getAllUsers({ size: 1 }), // Just to get total count
        api.getNovels({ size: 1 }), // Just to get total count
      ])

      if (usersResponse.success && novelsResponse.success) {
        setStats({
          totalUsers: usersResponse.data.totalElements,
          totalNovels: novelsResponse.data.totalElements,
          totalChapters: 1250, // Mock data
          totalViews: 45678, // Mock data
          activeUsers: 234, // Mock data
          newUsersToday: 12, // Mock data
          popularNovels: 45, // Mock data
          avgRating: 4.2, // Mock data
        })
      }
    } catch (error) {
      console.error("Failed to fetch dashboard stats:", error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-background">
      <main className="container mx-auto px-4 py-8">
        <div className="space-y-8">
          {/* Dashboard Header */}
          <div>
            <h1 className="text-3xl font-bold">Admin Dashboard</h1>
            <p className="text-muted-foreground">Manage your novel platform</p>
          </div>

          {/* Stats Overview */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Users</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{loading ? "..." : stats.totalUsers.toLocaleString()}</div>
                <p className="text-xs text-muted-foreground">+{stats.newUsersToday} new today</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Novels</CardTitle>
                <BookOpen className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{loading ? "..." : stats.totalNovels.toLocaleString()}</div>
                <p className="text-xs text-muted-foreground">{stats.popularNovels} trending</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Chapters</CardTitle>
                <FileText className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{loading ? "..." : stats.totalChapters.toLocaleString()}</div>
                <p className="text-xs text-muted-foreground">+25 published today</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Views</CardTitle>
                <Eye className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{loading ? "..." : stats.totalViews.toLocaleString()}</div>
                <p className="text-xs text-muted-foreground">+12% from last month</p>
              </CardContent>
            </Card>
          </div>

          {/* Additional Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Active Users</CardTitle>
                <TrendingUp className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{loading ? "..." : stats.activeUsers}</div>
                <p className="text-xs text-muted-foreground">Online now</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Average Rating</CardTitle>
                <Star className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{loading ? "..." : stats.avgRating}/5</div>
                <p className="text-xs text-muted-foreground">Platform average</p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Comments Today</CardTitle>
                <MessageCircle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">156</div>
                <p className="text-xs text-muted-foreground">+8% from yesterday</p>
              </CardContent>
            </Card>
          </div>

          {/* Quick Actions */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Users className="h-5 w-5" />
                  User Management
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">Manage user accounts, roles, and permissions</p>
                <div className="space-y-2">
                  <Link href="/admin/users" className="block">
                    <Button variant="outline" className="w-full justify-start bg-transparent">
                      Manage Users
                    </Button>
                  </Link>
                  <Link href="/admin/users/roles" className="block">
                    <Button variant="outline" className="w-full justify-start bg-transparent">
                      Role Approvals
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>

            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <BookOpen className="h-5 w-5" />
                  Content Management
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">Manage novels, chapters, genres, and tags</p>
                <div className="space-y-2">
                  <Link href="/admin/novels" className="block">
                    <Button variant="outline" className="w-full justify-start bg-transparent">
                      Manage Novels
                    </Button>
                  </Link>
                  <Link href="/admin/categories" className="block">
                    <Button variant="outline" className="w-full justify-start bg-transparent">
                      Manage Categories
                    </Button>
                  </Link>
                  <Link href="/admin/genres" className="block">
                    <Button variant="outline" className="w-full justify-start bg-transparent">
                      Manage Genres
                    </Button>
                  </Link>
                  <Link href="/admin/tags" className="block">
                    <Button variant="outline" className="w-full justify-start bg-transparent">
                      Manage Tags
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>

            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5" />
                  Analytics & Reports
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">View analytics, reports, and system insights</p>
                <div className="space-y-2">
                  <Link href="/admin/analytics" className="block">
                    <Button variant="outline" className="w-full justify-start bg-transparent">
                      System Analytics
                    </Button>
                  </Link>
                  <Link href="/admin/reports" className="block">
                    <Button variant="outline" className="w-full justify-start bg-transparent">
                      Reports
                    </Button>
                  </Link>
                  <Link href="/admin/comments" className="block">
                    <Button variant="outline" className="w-full justify-start bg-transparent">
                      Comment Moderation
                    </Button>
                  </Link>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* New Admin Features */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <FileText className="h-5 w-5" />
                  File Management
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">Manage uploaded files in the system</p>
                <Link href="/admin/files" className="block">
                  <Button variant="outline" className="w-full justify-start bg-transparent">
                    Manage Files
                  </Button>
                </Link>
              </CardContent>
            </Card>

            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Bell className="h-5 w-5" />
                  Notifications
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">Manage system-wide announcements and notifications</p>
                <Link href="/admin/notifications" className="block">
                  <Button variant="outline" className="w-full justify-start bg-transparent">
                    Notification Center
                  </Button>
                </Link>
              </CardContent>
            </Card>

            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <PlayCircle className="h-5 w-5" />
                  Video Library
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">Curate external videos and keep the catalog fresh</p>
                <Link href="/admin/videos" className="block">
                  <Button variant="outline" className="w-full justify-start bg-transparent">
                    Manage Videos
                  </Button>
                </Link>
              </CardContent>
            </Card>

            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5" />
                  Feature Requests
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">Review, approve, and manage feature requests</p>
                <Link href="/admin/feature-requests" className="block">
                  <Button variant="outline" className="w-full justify-start bg-transparent">
                    Feature Requests
                  </Button>
                </Link>
              </CardContent>
            </Card>

            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <MessageCircle className="h-5 w-5" />
                  Comments
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">Moderate and manage all user comments</p>
                <Link href="/admin/comments" className="block">
                  <Button variant="outline" className="w-full justify-start bg-transparent">
                    Comments Management
                  </Button>
                </Link>
              </CardContent>
            </Card>

            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Star className="h-5 w-5" />
                  Reviews
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">Moderate novel reviews and ratings</p>
                <Link href="/admin/reviews" className="block">
                  <Button variant="outline" className="w-full justify-start bg-transparent">
                    Reviews Management
                  </Button>
                </Link>
              </CardContent>
            </Card>

            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Users className="h-5 w-5" />
                  Messages
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">Monitor user messages and group chats</p>
                <Link href="/admin/messages" className="block">
                  <Button variant="outline" className="w-full justify-start bg-transparent">
                    Messages & Groups
                  </Button>
                </Link>
              </CardContent>
            </Card>

            <Card className="hover:shadow-lg transition-shadow">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <AlertCircle className="h-5 w-5" />
                  Reports
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <p className="text-sm text-muted-foreground">Handle content and user reports</p>
                <Link href="/admin/reports" className="block">
                  <Button variant="outline" className="w-full justify-start bg-transparent">
                    Reports Management
                  </Button>
                </Link>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </div>
  )
}
