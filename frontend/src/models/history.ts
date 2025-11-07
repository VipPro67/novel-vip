import { Novel } from "./novel"

export interface ReadingHistory {
  id: string
  userId: string
  novel: Novel
  lastReadChapterIndex: number
  lastReadAt: string
  createdAt: string
}
