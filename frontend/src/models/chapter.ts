import type { FileMetadata } from "./file"

export interface Chapter {
  id: string
  chapterNumber: number
  title: string
  novelId: string
  novelTitle: string
  createdAt: string
  updatedAt: string
}

export interface ChapterDetail extends Chapter {
  jsonUrl?: string
  audioUrl?: string
  jsonMetadata?: FileMetadata
  audioMetadata?: FileMetadata
  price?: number
  isLocked?: boolean
  isUnlocked?: boolean
}
