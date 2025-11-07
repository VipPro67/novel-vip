export interface Bookmark {
  id: string
  userId: string
  chapterId: string
  novelId: string
  chapterTitle: string
  novelTitle: string
  note?: string
  progress: number
  createdAt: string
  updatedAt: string
}
