export interface Report {
  id: string
  reportType: "NOVEL" | "CHAPTER" | "COMMENT" | "REVIEW" | "USER"
  targetId: string
  reason: "SPAM" | "INAPPROPRIATE_CONTENT" | "COPYRIGHT_VIOLATION" | "HARASSMENT" | "MISINFORMATION" | "OTHER"
  description: string
  status: "PENDING" | "REVIEWED" | "RESOLVED" | "DISMISSED"
  reportedBy: string
  createdAt: string
  updatedAt: string
}
