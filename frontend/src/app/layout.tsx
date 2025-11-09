import type React from "react"
import type { Metadata } from "next"
import { Inter } from "next/font/google"
import "./globals.css"
import { GoogleOAuthProvider } from "@react-oauth/google"
import { AuthProvider } from "@/components/providers/auth-provider"
import { ReaderSettingsProvider } from "@/components/providers/reader-settings-provider"
import { ThemeProvider } from "@/components/providers/theme-provider"
import { Toaster } from "@/components/ui/toaster"
import { LoginModal } from "@/components/auth/login-modal"
import { RegisterModal } from "@/components/auth/register-modal"
import { Header } from "@/components/layout/header"
import { ChatWidget } from "@/components/chat-widget"

const inter = Inter({ subsets: ["latin"] })

export const metadata: Metadata = {
  title: "Novel VIP Pro",
  description: "Premium novel reading platform",
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  const googleClientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID

  const appContent = (
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem disableTransitionOnChange>
      <AuthProvider>
        <ReaderSettingsProvider>
          <LoginModal />
          <RegisterModal />
          <main>
            <Header />
            {children}
          </main>
          <ChatWidget />
          <Toaster />
        </ReaderSettingsProvider>
      </AuthProvider>
    </ThemeProvider>
  )

  return (
    <html lang="en" suppressHydrationWarning>
      <body className={inter.className}>
        {googleClientId ? (
          <GoogleOAuthProvider clientId={googleClientId}>{appContent}</GoogleOAuthProvider>
        ) : (
          appContent
        )}
      </body>
    </html>
  )
}
