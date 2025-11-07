export enum ERole {
  USER = 0,
  MODERATOR = 1,
  ADMIN = 2,
  AUTHOR = 3,
}

export interface RoleRequest {
  id: string
  userId: string
  username: string
  email: string
  requestedRole: ERole
  reason: string
  status: "PENDING" | "APPROVED" | "REJECTED"
  createdAt: string
  updatedAt: string
  processedBy?: string
  rejectReason?: string
}
