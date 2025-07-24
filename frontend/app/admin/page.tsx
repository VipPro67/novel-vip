"use client"

import { useState, useEffect } from "react"
import { Users, BookOpen, FileText, TrendingUp, Eye, Star, MessageCircle } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { UserManagement } from "@/components/admin/user-management"
import { NovelManagement } from "@/components/admin/novel-management"
import { SystemAnalytics } from "@/components/admin/system-analytics"
import { useAuth } from "@/components/providers/auth-provider"
import { api } from "@/lib/api"
import { RoleApprovalManagement } from "@/components/admin/role-approval-management"

export default function AdminDashboard() {
  const { user, hasRole } = useAuth()

  console.log("Admin page - User:", user)
  console.log("Admin page - Has ADMIN role:", hasRole("ADMIN"))
  console.log("Admin page - User roles:", user?.roles)

  return (
    <AuthGuard requireAdmin>
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
    fetchDashboardStats()
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
      <Header />

      <main className="container py-8">
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

          {/* Management Tabs */}
          <Tabs defaultValue="users" className="space-y-4">
            <TabsList>
              <TabsTrigger value="users">User Management</TabsTrigger>
              <TabsTrigger value="novels">Novel Management</TabsTrigger>
              <TabsTrigger value="role-approvals">Role Approvals</TabsTrigger>
              <TabsTrigger value="analytics">Analytics</TabsTrigger>
            </TabsList>

            <TabsContent value="users" className="space-y-4">
              <UserManagement />
            </TabsContent>

            <TabsContent value="novels" className="space-y-4">
              <NovelManagement />
            </TabsContent>

            <TabsContent value="role-approvals" className="space-y-4">
              <RoleApprovalManagement />
            </TabsContent>

            <TabsContent value="analytics" className="space-y-4">
              <SystemAnalytics />
            </TabsContent>
          </Tabs>
        </div>
      </main>
    </div>
  )
}
