"use client"

import type React from "react"
import { useState } from "react"
import { useRouter } from "@/navigation"
import { Eye, EyeOff, BookOpen } from "lucide-react"
import { GoogleLogin, GoogleOAuthProvider, type CredentialResponse } from "@react-oauth/google"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { useToast } from "@/hooks/use-toast"
import { useAuth } from "@/components/providers/auth-provider"
import { useAuthModals } from "@/hooks/use-auth-modals"

export function LoginModal() {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const { login, loginWithGoogle } = useAuth()
  const { toast } = useToast()
  const router = useRouter()
  const { loginOpen, closeLogin, switchToRegister } = useAuthModals()
  const googleEnabled = Boolean(process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID)


  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      const response = await login(email, password)

      if (response?.success) {
        toast({
          title: "Login successful",
          description: "Welcome back!",
        })
        closeLogin()
        router.refresh()
      } else {
        console.error("Login failed:", response?.message)
        toast({
          title: "Login failed",
          description: response?.message || "Invalid email or password",
          variant: "destructive",
        })
      }
    } catch (error: any) {
      const message = error?.response?.data?.message || error.message || "An error occurred"
      toast({
        title: "Login failed",
        description: message,
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleGoogleSuccess = async (credentialResponse: CredentialResponse) => {
    if (!credentialResponse.credential) {
      toast({
        title: "Google login failed",
        description: "Unable to retrieve Google credentials",
        variant: "destructive",
      })
      return
    }

    setLoading(true)
    try {
      const response = await loginWithGoogle(credentialResponse.credential)
      if (response?.success) {
        toast({ title: "Login successful", description: "Welcome back!" })
        closeLogin()
        router.refresh()
      } else {
        toast({
          title: "Google login failed",
          description: response?.message || "Unable to authenticate with Google",
          variant: "destructive",
        })
      }
    } catch (error: any) {
      const message = error?.message || "Unable to authenticate with Google"
      toast({ title: "Google login failed", description: message, variant: "destructive" })
    } finally {
      setLoading(false)
    }
  }

  const handleGoogleError = () => {
    toast({
      title: "Google login failed",
      description: "The Google sign-in popup was closed or an error occurred",
      variant: "destructive",
    })
  }

  return (
    <Dialog open={loginOpen} onOpenChange={closeLogin}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <BookOpen className="h-6 w-6 text-primary" />
              <DialogTitle>Welcome Back</DialogTitle>
            </div>
          </div>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              type="email"
              placeholder="Enter your email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <div className="relative">
              <Input
                id="password"
                type={showPassword ? "text" : "password"}
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
              <Button
                type="button"
                variant="ghost"
                size="icon"
                className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </Button>
            </div>
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? "Signing in..." : "Sign In"}
          </Button>
        </form>

        {googleEnabled && (
          <div className="space-y-4">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <span className="w-full border-t" />
              </div>
              <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-background px-2 text-muted-foreground">Or continue with</span>
              </div>
            </div>
            <div className={loading ? "pointer-events-none opacity-60" : ""}>
              <div className="flex justify-center">
                <GoogleOAuthProvider clientId={process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID}>
                  <GoogleLogin onSuccess={handleGoogleSuccess} onError={handleGoogleError} useOneTap={false} width="100%" />
                </GoogleOAuthProvider>
              </div>
            </div>
          </div>
        )}

        <div className="text-center text-sm">
          <span className="text-muted-foreground">Don't have an account? </span>
          <button onClick={switchToRegister} className="text-primary hover:underline font-medium">
            Sign up
          </button>
        </div>
      </DialogContent>
    </Dialog>
  )
}
