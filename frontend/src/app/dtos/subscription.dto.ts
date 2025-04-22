export interface SubscriptionDTO {
  id: string;
  userId: string;
  username: string;
  planId: string;
  planName: string;
  status: string;
  startDate: string;
  endDate: string;
  paymentMethod: string;
  createdAt: string;
  updatedAt: string;
}

export interface SubscriptionPlanDTO {
  id: string;
  planId: string;
  name: string;
  description: string;
  price: number;
  currency: string;
  billingPeriod: string;
  features: string[];
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface SubscriptionHistoryDTO {
  id: string;
  userId: string;
  username: string;
  planId: string;
  planName: string;
  status: string;
  startDate: string;
  endDate: string;
  paymentMethod: string;
  paymentStatus: string;
  createdAt: string;
  updatedAt: string;
}

export interface PageResponseSubscriptionHistoryDTO {
  totalPages: number;
  totalElements: number;
  pageNumber: number;
  pageSize: number;
  content: SubscriptionHistoryDTO[];
} 