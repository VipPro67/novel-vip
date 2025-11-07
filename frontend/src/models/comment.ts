export interface Comment {
  id: string
  content: string
  userId: string
  username: string
  novelId?: string
  chapterId?: string
  parentId?: string | null
  replies?: Comment[]
  createdAt: string
  updatedAt: string
  edited?: boolean
}
