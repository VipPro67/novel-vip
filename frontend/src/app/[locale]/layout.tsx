import type { ReactNode } from "react"
import { GoogleOAuthProvider } from "@react-oauth/google"
import { notFound } from "next/navigation"
import { NextIntlClientProvider } from "next-intl"
import { getLocale, getMessages, setRequestLocale } from "next-intl/server"
import { locales, type Locale, defaultLocale } from "@/i18n/config"
import { ThemeProvider } from "@/components/providers/theme-provider"
import { AuthProvider } from "@/components/providers/auth-provider"
import { ReaderSettingsProvider } from "@/components/providers/reader-settings-provider"
import { LoginModal } from "@/components/auth/login-modal"
import { RegisterModal } from "@/components/auth/register-modal"
import { Header } from "@/components/layout/header"
import { ChatWidget } from "@/components/chat-widget"
import { Toaster } from "@/components/ui/toaster"

type LocaleLayoutProps = {
  children: ReactNode
}

export function generateStaticParams() {
  return locales.map((locale) => ({ locale }))
}

export default async function LocaleLayout({ children }: LocaleLayoutProps) {
  const detectedLocale = await getLocale()
  const localeCandidate = (detectedLocale ?? defaultLocale) as Locale
  if (!locales.includes(localeCandidate)) {
    notFound()
  }
  const locale = localeCandidate

  setRequestLocale(locale)

  const messages = await getMessages()
  const googleClientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID

  const content = (
    <NextIntlClientProvider locale={locale} messages={messages}>
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
    </NextIntlClientProvider>
  )

  if (!googleClientId) {
    return content
  }

  return <GoogleOAuthProvider clientId={googleClientId}>{content}</GoogleOAuthProvider>
}
