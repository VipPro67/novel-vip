package com.novel.vippro.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaymentStatsDTO {
    private BigDecimal totalRevenue;
    private BigDecimal totalRefunded;
    private long totalTransactions;
    private long successfulTransactions;
    private long failedTransactions;
    private long refundedTransactions;
    private Map<String, BigDecimal> revenueByPaymentMethod;
    private Map<String, Long> transactionsByStatus;
}