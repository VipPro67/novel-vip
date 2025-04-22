export interface ReportDTO {
  id: string;
  reporterId: string;
  reporterUsername: string;
  novelId: string;
  novelTitle: string;
  chapterId?: string;
  chapterTitle?: string;
  commentId?: string;
  reason: string;
  description: string;
  status: ReportStatus;
  adminResponse?: string;
  createdAt: string;
  resolvedAt?: string;
}

export enum ReportStatus {
  PENDING = 'PENDING',
  RESOLVED = 'RESOLVED',
  REJECTED = 'REJECTED'
}

export interface ReportCreateDTO {
  novelId?: string;
  chapterId?: string;
  commentId?: string;
  reason: string;
  description?: string;
}

export interface ReportUpdateDTO {
  status: ReportStatus;
  adminResponse?: string;
}

export interface PageResponseReportDTO {
  totalPages: number;
  totalElements: number;
  pageNumber: number;
  pageSize: number;
  content: ReportDTO[];
} 