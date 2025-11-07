export interface ReadingHistory {
  id: string
  userId: string
  novelId: string
  novelTitle: string
  novelCover?: string
  chapterId: string
  chapterTitle: string
  chapterNumber: number
  progress: number
  readingTime: number
  lastReadAt: string
  createdAt: string
}
