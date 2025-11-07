export interface Video {
  id: string
  title: string
  description?: string
  videoUrl: string
  embedUrl: string
  platform: "YOUTUBE" | "FACEBOOK"
  externalId?: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateVideoPayload {
  title: string
  description?: string
  videoUrl: string
}
