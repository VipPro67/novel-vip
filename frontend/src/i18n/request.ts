import { getRequestConfig } from "next-intl/server"
import { defaultLocale, locales, type Locale } from "./config"

export default getRequestConfig(async ({ locale }) => {
  const resolvedLocale = (locales.includes(locale as Locale) ? locale : defaultLocale) as Locale

  return {
    locale: resolvedLocale,
    messages: (await import(`../messages/${resolvedLocale}.json`)).default,
  }
})
