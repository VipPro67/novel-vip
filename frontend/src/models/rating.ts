export interface Rating {
  id: string
  userId: string
  username: string
  novelId: string
  novelTitle: string
  score: number
  review?: string
  createdAt: string
  updatedAt: string
}
