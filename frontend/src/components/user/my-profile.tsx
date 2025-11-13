"use client"

import type React from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Label } from "@/components/ui/label"
import { Input } from "../ui/input"
import { useAuth } from "../providers/auth-provider"
import { Badge } from "../ui/badge"
import { useTranslations } from "next-intl"

export function MyProfile(){
  const t = useTranslations("Profile")
	const { user } = useAuth()

	if (!user) return null
  return (
    <Card>
      <CardHeader>
        <CardTitle>{t("title")}</CardTitle>
        <CardDescription>{t('manageProfileInformation')}</CardDescription>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          <div>
            <Label htmlFor="username">{t('username')}</Label>
            <Input id="username" value={user.fullName || user.username} disabled />
          </div>
          <div>
            <Label htmlFor="email">{t('email')}</Label>
            <Input id="email" value={user.email} disabled />
          </div>
          <div>
            <Label>{t('roles')}</Label>
            <div className="flex flex-wrap gap-2">
              {user.roles.map((role) => (
                <Badge key={role} variant="secondary">
                  {role}
                </Badge>
              ))}
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
