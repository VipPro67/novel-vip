export interface FeatureRequest {
  id: string
  title: string
  description: string
  status: "PENDING" | "UNDER_REVIEW" | "PLANNED" | "IN_PROGRESS" | "COMPLETED" | "REJECTED"
  category: string
  priority: "LOW" | "MEDIUM" | "HIGH"
  voteCount: number
  hasVoted: boolean
  createdBy: string
  createdByUsername: string
  createdAt: string
  updatedAt: string
}