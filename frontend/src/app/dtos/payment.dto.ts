export interface PaymentDTO {
  id: string;
  userId: string;
  username: string;
  amount: number;
  currency: string;
  status: string;
  paymentMethod: string;
  paymentGatewayId: string;
  subscriptionId?: string;
  description?: string;
  errorMessage?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentCreateDTO {
  amount: number;
  currency: string;
  paymentMethod: string;
  subscriptionId?: string;
  description?: string;
}

export interface PaymentStatsDTO {
  totalRevenue: number;
  totalRefunded: number;
  totalTransactions: number;
  successfulTransactions: number;
  failedTransactions: number;
  refundedTransactions: number;
  revenueByPaymentMethod: { [key: string]: number };
  transactionsByStatus: { [key: string]: number };
}

export interface PageResponsePaymentDTO {
  totalPages: number;
  totalElements: number;
  pageNumber: number;
  pageSize: number;
  content: PaymentDTO[];
} 