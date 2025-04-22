export type FeatureRequestStatus = 'VOTING' | 'PROCESSING' | 'DONE' | 'REJECTED';

export interface FeatureRequestDTO {
  id: number;
  title: string;
  description: string;
  userId: string;
  username: string;
  status: FeatureRequestStatus;
  voteCount: number;
  hasVoted: boolean;
}

export interface CreateFeatureRequestDTO {
  title: string;
  description: string;
}

export interface PageResponseFeatureRequestDTO {
  totalPages: number;
  totalElements: number;
  pageNumber: number;
  pageSize: number;
  content: FeatureRequestDTO[];
} 