import createMiddleware from "next-intl/middleware"
import { defaultLocale, localePrefix, locales } from "./src/i18n/config"

export default createMiddleware({
  locales,
  defaultLocale,
  localePrefix,
})

export const config = {
  matcher: ["/", "/(vi|en)/:path*", "/((?!api|_next|_vercel|.*\\..*).*)"],
}
