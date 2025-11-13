import type { ReactNode } from "react"
import { GoogleOAuthProvider } from "@react-oauth/google"
import { notFound } from "next/navigation"
import { NextIntlClientProvider } from "next-intl"
import { getMessages, setRequestLocale } from "next-intl/server"
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
  params: Promise<{ locale: string }>
}

export function generateStaticParams() {
  return locales.map((locale) => ({ locale }))
}

export default async function LocaleLayout({ children, params }: LocaleLayoutProps) {
  const { locale: localeParam } = await params

  if (!locales.includes(localeParam as Locale)) {
    notFound()
  }

  const locale = localeParam as Locale

  setRequestLocale(locale)

  const messages = await getMessages({ locale })
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
