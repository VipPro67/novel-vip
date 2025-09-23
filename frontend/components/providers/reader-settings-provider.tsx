"use client"

import type React from "react"
import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react"

import { api, type ReaderSettings } from "@/lib/api"
import { useAuth } from "./auth-provider"

interface ReaderSettingsContextValue {
  settings: ReaderSettings | null
  loading: boolean
  saving: boolean
  error: string | null
  refreshSettings: (options?: { silent?: boolean }) => Promise<ReaderSettings | null>
  updateSettings: (changes: Partial<ReaderSettings>) => Promise<ReaderSettings | null>
}

const ReaderSettingsContext = createContext<ReaderSettingsContextValue | undefined>(undefined)
const STORAGE_KEY = "readerSettings"

export function ReaderSettingsProvider({ children }: { children: React.ReactNode }) {
  const { user, isAuthenticated, loading: authLoading } = useAuth()
  const [settings, setSettings] = useState<ReaderSettings | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const cacheSettings = useCallback((value: ReaderSettings | null) => {
    if (typeof window === "undefined") {
      return
    }

    if (!value) {
      window.localStorage.removeItem(STORAGE_KEY)
      return
    }

    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
  }, [])

  const loadCachedSettings = useCallback((): ReaderSettings | null => {
    if (typeof window === "undefined" || !user) {
      return null
    }

    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) {
      return null
    }

    try {
      const parsed = JSON.parse(raw) as ReaderSettings
      if (parsed.userId !== user.id) {
        window.localStorage.removeItem(STORAGE_KEY)
        return null
      }
      return parsed
    } catch (parseError) {
      console.error("Failed to parse cached reader settings", parseError)
      window.localStorage.removeItem(STORAGE_KEY)
      return null
    }
  }, [user])

  const loadSettings = useCallback(
    async (options?: { silent?: boolean }): Promise<ReaderSettings | null> => {
      if (!isAuthenticated || !user) {
        setSettings(null)
        cacheSettings(null)
        setError(null)
        setLoading(false)
        return null
      }

      if (!options?.silent) {
        setLoading(true)
      }

      try {
        const response = await api.getReaderSettings()
        if (!response.success) {
          throw new Error(response.message || "Failed to load reader settings")
        }

        setSettings(response.data)
        cacheSettings(response.data)
        setError(null)
        return response.data
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to load reader settings"
        console.error("Failed to load reader settings", err)
        setError(message)
        throw new Error(message)
      } finally {
        if (!options?.silent) {
          setLoading(false)
        }
      }
    },
    [cacheSettings, isAuthenticated, user]
  )

  useEffect(() => {
    if (authLoading) {
      setLoading(true)
      return
    }

    if (!isAuthenticated || !user) {
      setSettings(null)
      cacheSettings(null)
      setError(null)
      setLoading(false)
      return
    }

    const cached = loadCachedSettings()
    if (cached) {
      setSettings(cached)
      setLoading(false)
    } else {
      setLoading(true)
    }

    loadSettings({ silent: Boolean(cached) }).catch((err) => {
      console.error("Failed to refresh reader settings", err)
      if (!cached) {
        setLoading(false)
      }
    })
  }, [authLoading, cacheSettings, isAuthenticated, loadCachedSettings, loadSettings, user])

  const updateSettings = useCallback(
    async (changes: Partial<ReaderSettings>): Promise<ReaderSettings | null> => {
      if (!isAuthenticated || !user) {
        throw new Error("You need to be logged in to update reader settings.")
      }

      setSaving(true)
      try {
        const response = await api.updateReaderSettings(changes)
        if (!response.success) {
          throw new Error(response.message || "Failed to update reader settings")
        }

        setSettings(response.data)
        cacheSettings(response.data)
        setError(null)
        return response.data
      } catch (err) {
        const message = err instanceof Error ? err.message : "Failed to update reader settings"
        console.error("Failed to update reader settings", err)
        setError(message)
        throw new Error(message)
      } finally {
        setSaving(false)
      }
    },
    [cacheSettings, isAuthenticated, user]
  )

  const refreshSettings = useCallback(
    async (options?: { silent?: boolean }): Promise<ReaderSettings | null> => {
      return loadSettings(options)
    },
    [loadSettings]
  )

  const contextValue = useMemo<ReaderSettingsContextValue>(
    () => ({
      settings,
      loading,
      saving,
      error,
      refreshSettings,
      updateSettings,
    }),
    [error, loading, refreshSettings, saving, settings, updateSettings]
  )

  return <ReaderSettingsContext.Provider value={contextValue}>{children}</ReaderSettingsContext.Provider>
}

export function useReaderSettings() {
  const context = useContext(ReaderSettingsContext)
  if (!context) {
    throw new Error("useReaderSettings must be used within a ReaderSettingsProvider")
  }
  return context
}
