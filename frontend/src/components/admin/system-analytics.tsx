"use client"

import { useState } from "react"
import { TrendingUp, Users, BookOpen, Eye, MessageCircle } from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"

export function SystemAnalytics() {
  const [analytics, setAnalytics] = useState({
    userGrowth: [
      { month: "Jan", users: 120 },
      { month: "Feb", users: 150 },
      { month: "Mar", users: 180 },
      { month: "Apr", users: 220 },
      { month: "May", users: 280 },
      { month: "Jun", users: 350 },
    ],
    topGenres: [
      { name: "Fantasy", count: 45, percentage: 35 },
      { name: "Romance", count: 38, percentage: 30 },
      { name: "Sci-Fi", count: 25, percentage: 20 },
      { name: "Mystery", count: 19, percentage: 15 },
    ],
    recentActivity: [
      { type: "user_registration", count: 12, change: "+8%" },
      { type: "novel_published", count: 3, change: "+2%" },
      { type: "chapters_added", count: 25, change: "+15%" },
      { type: "comments_posted", count: 156, change: "+12%" },
    ],
  })

  return (
    <div className="space-y-6">
      {/* User Growth */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <TrendingUp className="h-5 w-5" />
            <span>User Growth</span>
          </CardTitle>
          <CardDescription>Monthly user registration trends</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {analytics.userGrowth.map((data, index) => (
              <div key={data.month} className="flex items-center space-x-4">
                <div className="w-12 text-sm font-medium">{data.month}</div>
                <div className="flex-1">
                  <Progress value={(data.users / 350) * 100} className="h-2" />
                </div>
                <div className="w-16 text-sm text-right">{data.users}</div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Top Genres */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <BookOpen className="h-5 w-5" />
            <span>Popular Genres</span>
          </CardTitle>
          <CardDescription>Most popular novel genres on the platform</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {analytics.topGenres.map((genre) => (
              <div key={genre.name} className="flex items-center space-x-4">
                <div className="w-20 text-sm font-medium">{genre.name}</div>
                <div className="flex-1">
                  <Progress value={genre.percentage} className="h-2" />
                </div>
                <div className="w-16 text-sm text-right">
                  {genre.count} ({genre.percentage}%)
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Recent Activity */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Eye className="h-5 w-5" />
            <span>Recent Activity</span>
          </CardTitle>
          <CardDescription>Platform activity in the last 24 hours</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <Users className="h-4 w-4 text-blue-500" />
                  <span className="text-sm">New Users</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="font-medium">12</span>
                  <span className="text-xs text-green-600">+8%</span>
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <BookOpen className="h-4 w-4 text-purple-500" />
                  <span className="text-sm">Novels Published</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="font-medium">3</span>
                  <span className="text-xs text-green-600">+2%</span>
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <Eye className="h-4 w-4 text-orange-500" />
                  <span className="text-sm">Chapters Added</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="font-medium">25</span>
                  <span className="text-xs text-green-600">+15%</span>
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <MessageCircle className="h-4 w-4 text-green-500" />
                  <span className="text-sm">Comments Posted</span>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="font-medium">156</span>
                  <span className="text-xs text-green-600">+12%</span>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* System Health */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Server Status</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center space-x-2">
              <div className="h-2 w-2 bg-green-500 rounded-full"></div>
              <span className="text-sm">Online</span>
            </div>
            <p className="text-xs text-muted-foreground mt-1">99.9% uptime</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Database</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center space-x-2">
              <div className="h-2 w-2 bg-green-500 rounded-full"></div>
              <span className="text-sm">Healthy</span>
            </div>
            <p className="text-xs text-muted-foreground mt-1">Response time: 45ms</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Storage</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              <Progress value={65} className="h-2" />
              <p className="text-xs text-muted-foreground">65% used (2.1GB / 3.2GB)</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
