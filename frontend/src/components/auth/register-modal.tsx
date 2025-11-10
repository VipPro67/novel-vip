  "use client"

import type React from "react"
import { useState } from "react"
import { useRouter } from "next/navigation"
import { Eye, EyeOff, BookOpen } from "lucide-react"
import { GoogleLogin, type CredentialResponse } from "@react-oauth/google"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { useToast } from "@/hooks/use-toast"
import { useAuth } from "@/components/providers/auth-provider"
import { useAuthModals } from "@/hooks/use-auth-modals"

export function RegisterModal() {
  const [username, setUsername] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const { register, loginWithGoogle } = useAuth()
  const { toast } = useToast()
  const router = useRouter()
  const { registerOpen, closeRegister, switchToLogin } = useAuthModals()
  const googleEnabled = Boolean(process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (password !== confirmPassword) {
      toast({
        title: "Password mismatch",
        description: "Passwords do not match",
        variant: "destructive",
      })
      return
    }

    setLoading(true)

    try {
      const result = await register(username, email, password)

      if (result?.success) {
        toast({
          title: "Verify your email",
          description: `We sent a confirmation link to ${email}. Please verify before signing in.`,
        })
        closeRegister()
        switchToLogin()
      } else {
        toast({
          title: "Registration failed",
          description: result?.message || "Username or email already exists",
          variant: "destructive",
        })
      }
    } catch (error: any) {
      toast({
        title: "Registration failed",
        description: error?.message || "An error occurred. Please try again.",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleGoogleSuccess = async (credentialResponse: CredentialResponse) => {
    if (!credentialResponse.credential) {
      toast({
        title: "Google sign up failed",
        description: "Unable to retrieve Google credentials",
        variant: "destructive",
      })
      return
    }

    setLoading(true)
    try {
      const response = await loginWithGoogle(credentialResponse.credential)
      if (response?.success) {
        toast({ title: "Signed in with Google", description: "Welcome to Novel VIP!" })
        closeRegister()
        router.refresh()
      } else {
        toast({
          title: "Google sign up failed",
          description: response?.message || "Unable to complete Google authentication",
          variant: "destructive",
        })
      }
    } catch (error: any) {
      const message = error?.message || "Unable to authenticate with Google"
      toast({ title: "Google sign up failed", description: message, variant: "destructive" })
    } finally {
      setLoading(false)
    }
  }

  const handleGoogleError = () => {
    toast({
      title: "Google sign up failed",
      description: "The Google window was closed or an error occurred",
      variant: "destructive",
    })
  }

  return (
    <Dialog open={registerOpen} onOpenChange={closeRegister}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <div className="flex items-center space-x-2">
            <BookOpen className="h-6 w-6 text-primary" />
            <DialogTitle>Create Account</DialogTitle>
          </div>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="username">Username</Label>
            <Input
              id="username"
              type="text"
              placeholder="Choose a username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              minLength={3}
              maxLength={20}
            />
          </div>

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
                placeholder="Create a password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={6}
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

          <div className="space-y-2">
            <Label htmlFor="confirmPassword">Confirm Password</Label>
            <Input
              id="confirmPassword"
              type="password"
              placeholder="Confirm your password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>

          <Button type="submit" className="w-full" disabled={loading}>
            {loading ? "Creating account..." : "Create Account"}
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
              <GoogleLogin onSuccess={handleGoogleSuccess} onError={handleGoogleError} useOneTap={false} width="100%" />
            </div>
          </div>
        )}

        <div className="text-center text-sm">
          <span className="text-muted-foreground">Already have an account? </span>
          <button onClick={switchToLogin} className="text-primary hover:underline font-medium">
            Sign in
          </button>
        </div>
      </DialogContent>
    </Dialog>
  )
}
