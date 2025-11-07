export interface Review {
  id: string
  novelId: string
  novelTitle: string
  userId: string
  username: string
  userAvatar?: string
  title: string
  content: string
  rating: number
  helpfulVotes: number
  unhelpfulVotes: number
  edited: boolean
  verifiedPurchase: boolean
  createdAt: string
  updatedAt: string
}
