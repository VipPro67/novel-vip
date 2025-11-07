export interface Notification {
  id: string
  userId: string
  title: string
  message: string
  read: boolean
  type: "SYSTEM" | "BOOK_UPDATE" | "CHAPTER_UPDATE" | "COMMENT" | "LIKE" | "FOLLOW" | "MESSAGE"
  reference?: string
  createdAt: string
}
