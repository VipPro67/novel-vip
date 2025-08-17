"use client"

import { useState } from "react"
import { User, Shield, Bell, Palette } from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Header } from "@/components/layout/header"
import { AuthGuard } from "@/components/auth/auth-guard"
import { RoleRequestForm } from "@/components/user/role-request-form"
import { MyRoleRequests } from "@/components/user/my-role-requests"

export default function SettingsPage() {
  return (
    <AuthGuard>
      <SettingsContent />
    </AuthGuard>
  )
}

function SettingsContent() {
  const [refreshRequests, setRefreshRequests] = useState(0)

  const handleRequestSubmitted = () => {
    setRefreshRequests((prev) => prev + 1)
  }

  return (
    <div className="min-h-screen bg-background">
      <Header />

      <main className="container py-8">
        <div className="max-w-4xl mx-auto space-y-8">
          {/* Settings Header */}
          <div>
            <h1 className="text-3xl font-bold">Settings</h1>
            <p className="text-muted-foreground">Manage your account settings and preferences</p>
          </div>

          {/* Settings Tabs */}
          <Tabs defaultValue="profile" className="space-y-4">
            <TabsList className="grid w-full grid-cols-4">
              <TabsTrigger value="profile" className="flex items-center space-x-2">
                <User className="h-4 w-4" />
                <span>Profile</span>
              </TabsTrigger>
              <TabsTrigger value="roles" className="flex items-center space-x-2">
                <Shield className="h-4 w-4" />
                <span>Roles</span>
              </TabsTrigger>
              <TabsTrigger value="notifications" className="flex items-center space-x-2">
                <Bell className="h-4 w-4" />
                <span>Notifications</span>
              </TabsTrigger>
              <TabsTrigger value="appearance" className="flex items-center space-x-2">
                <Palette className="h-4 w-4" />
                <span>Appearance</span>
              </TabsTrigger>
            </TabsList>

            <TabsContent value="profile" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>Profile Settings</CardTitle>
                  <CardDescription>Manage your personal information</CardDescription>
                </CardHeader>
                <CardContent>
                  
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="roles" className="space-y-4">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <RoleRequestForm onRequestSubmitted={handleRequestSubmitted} />
                <div key={refreshRequests}>
                  <MyRoleRequests />
                </div>
              </div>
            </TabsContent>

            <TabsContent value="notifications" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>Notification Settings</CardTitle>
                  <CardDescription>Configure how you receive notifications</CardDescription>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">Notification settings will be implemented here.</p>
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="appearance" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>Appearance Settings</CardTitle>
                  <CardDescription>Customize the look and feel</CardDescription>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground">Appearance settings will be implemented here.</p>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>
      </main>
    </div>
  )
}
