import type { ReaderSettings } from "@/models"
import type { ApiClient } from "../api-client"

export const createReaderSettingsApi = (client: ApiClient) => ({
  async getReaderSettings() {
    return client.request<ReaderSettings>("/api/reader-settings")
  },

  async updateReaderSettings(settings: Partial<ReaderSettings>) {
    return client.request<ReaderSettings>("/api/reader-settings", {
      method: "PUT",
      body: JSON.stringify(settings),
    })
  },

  async getThemeOptions() {
    return client.request<string[]>("/api/reader-settings/themes")
  },

  async getFontOptions() {
    return client.request<string[]>("/api/reader-settings/fonts")
  },
})
