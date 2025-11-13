"use client"

import type React from "react"

import { useState } from "react"
import { Send, Shield } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { Label } from "@/components/ui/label"
import { useToast } from "@/hooks/use-toast"
import { api } from "@/services/api"
import { useTranslations } from "next-intl"

interface RoleRequestFormProps {
  onRequestSubmitted?: () => void
}

export function RoleRequestForm({ onRequestSubmitted }: RoleRequestFormProps) {
  const t = useTranslations("Profile")
  const [requestedRole, setRequestedRole] = useState("")
  const [reason, setReason] = useState("")
  const [loading, setLoading] = useState(false)
  const { toast } = useToast()

  const availableRoles = [
    { value: "AUTHOR", label: "Author", description: "Create and publish novels" },
    { value: "MODERATOR", label: "Moderator", description: "Moderate content and users" },
    //{ value: "ADMIN", label: "Admin", description: "Full system administration" },
  ]

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!requestedRole || !reason.trim()) {
      toast({
        title: "Validation Error",
        description: "Please select a role and provide a reason",
        variant: "destructive",
      })
      return
    }

    setLoading(true)

    try {
      const response = await api.requestRole(requestedRole, reason.trim())
      if (response.success) {
        toast({
          title: "Request Submitted",
          description: "Your role request has been submitted for review",
        })
        setRequestedRole("")
        setReason("")
        onRequestSubmitted?.()
      }
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to submit role request. Please try again.",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center space-x-2">
          <Shield className="h-5 w-5" />
          <span>{t('roleRequestForm.title')}</span>
        </CardTitle>
        <CardDescription>{t('roleRequestForm.description')}</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="role">{t('roleRequestForm.requestedRole.label')}</Label>
            <Select value={requestedRole} onValueChange={setRequestedRole}>
              <SelectTrigger>
                <SelectValue placeholder={t('roleRequestForm.requestedRole.placeholder')} />
              </SelectTrigger>
              <SelectContent>
                {availableRoles.map((role) => (
                  <SelectItem key={role.value} value={role.value}>
                    <div className="flex flex-col">
                      <span className="font-medium">{role.label}</span>
                      <span className="text-xs text-muted-foreground">{role.description}</span>
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="reason">{t('roleRequestForm.reason.label')}</Label>
            <Textarea
              id="reason"
              placeholder="Please explain why you need this role and how you plan to use it..."
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              rows={4}
              maxLength={500}
            />
            <p className="text-xs text-muted-foreground">{reason.length}/500 {t('roleRequestForm.reason.characterCount')}</p>
          </div>

          <Button type="submit" disabled={loading} className="w-full">
            {loading ? (
              "Submitting..."
            ) : (
              <>
                <Send className="mr-2 h-4 w-4" />
               {t('roleRequestForm.submitButton.text')}
              </>
            )}
          </Button>
        </form>
      </CardContent>
    </Card>
  )
}
