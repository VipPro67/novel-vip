export interface CorrectionRequest {
  id: string
  userId: string
  novelId: string
  chapterId: string
  chapterNumber: number
  charIndex?: number
  paragraphIndex?: number
  originalText: string
  suggestedText: string
  reason?: string
  status: "PENDING" | "APPROVED" | "REJECTED"
  rejectionReason?: string
  previousParagraph?: string
  paragraphText?: string
  nextParagraph?: string
  createdAt: string
  updatedAt: string
}

export interface CorrectionRequestWithDetails extends CorrectionRequest {
  user?: {
    id: string
    username: string
    email: string
  }
  novel?: {
    id: string
    title: string
    slug: string
  }
  chapter?: {
    id: string
    title: string
  }
}