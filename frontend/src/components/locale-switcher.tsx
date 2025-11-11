"use client"

import { useTransition } from "react"
import { useLocale, useTranslations } from "next-intl"
import { usePathname, useRouter } from "@/navigation"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { type Locale } from "@/i18n/config"

const localeOptions: { value: Locale; translationKey: "vi" | "en" }[] = [
  { value: "vi", translationKey: "vi" },
  { value: "en", translationKey: "en" },
]

export function LocaleSwitcher() {
  const locale = useLocale() as Locale
  const router = useRouter()
  const pathname = usePathname()
  const t = useTranslations("LocaleSwitcher")
  const [isPending, startTransition] = useTransition()

  const handleLocaleChange = (nextLocale: Locale) => {
    if (nextLocale === locale) {
      return
    }
    startTransition(() => {
      router.replace(pathname, { locale: nextLocale })
    })
  }

  return (
    <Select value={locale} onValueChange={(value) => handleLocaleChange(value as Locale)} disabled={isPending}>
      <SelectTrigger className="w-[130px]" aria-label={t("label")}>
        <SelectValue placeholder={t("label")} />
      </SelectTrigger>
      <SelectContent>
        {localeOptions.map((option) => (
          <SelectItem key={option.value} value={option.value}>
            {t(`languages.${option.translationKey}`)}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  )
}
