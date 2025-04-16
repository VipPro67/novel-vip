package com.novel.vippro.services.impl;

import com.novel.vippro.dto.PaymentDTO;
import com.novel.vippro.dto.PaymentCreateDTO;
import com.novel.vippro.dto.PaymentStatsDTO;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.exception.BadRequestException;
import com.novel.vippro.models.Payment;
import com.novel.vippro.models.User;
import com.novel.vippro.repository.PaymentRepository;
import com.novel.vippro.repository.UserRepository;
import com.novel.vippro.services.PaymentService;
import com.novel.vippro.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public PaymentDTO createPayment(PaymentCreateDTO paymentDTO) {
        UUID userId = userService.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(paymentDTO.getAmount());
        payment.setCurrency(paymentDTO.getCurrency());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setSubscriptionId(paymentDTO.getSubscriptionId());
        payment.setDescription(paymentDTO.getDescription());
        payment.setStatus("PENDING");

        // TODO: Integrate with actual payment gateway
        // For now, simulate payment processing
        try {
            // Simulate payment gateway call
            Thread.sleep(1000);
            payment.setStatus("COMPLETED");
            payment.setPaymentGatewayId("pg_" + UUID.randomUUID().toString());
            payment.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            payment.setStatus("FAILED");
            payment.setErrorMessage(e.getMessage());
        }

        Payment savedPayment = paymentRepository.save(payment);
        return convertToDTO(savedPayment);
    }

    @Override
    public Page<PaymentDTO> getUserPayments(Pageable pageable) {
        UUID userId = userService.getCurrentUserId();
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public PaymentDTO getPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        // Verify ownership
        UUID currentUserId = userService.getCurrentUserId();
        if (!payment.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("Not authorized to view this payment");
        }

        return convertToDTO(payment);
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {
        // TODO: Implement webhook handling with payment gateway
        // For now, just log the webhook
        System.out.println("Received webhook: " + payload);
        System.out.println("Signature: " + signature);
    }

    @Override
    @Transactional
    public void cancelPayment(UUID id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));

        // Verify ownership
        UUID currentUserId = userService.getCurrentUserId();
        if (!payment.getUser().getId().equals(currentUserId)) {
            throw new BadRequestException("Not authorized to cancel this payment");
        }

        if (!"PENDING".equals(payment.getStatus())) {
            throw new BadRequestException("Only pending payments can be cancelled");
        }

        payment.setStatus("CANCELLED");
        paymentRepository.save(payment);
    }

    @Override
    public PaymentStatsDTO getPaymentStats(String startDate, String endDate) {
        LocalDateTime start = startDate != null
                ? LocalDateTime.parse(startDate + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : LocalDateTime.now().minusMonths(1);

        LocalDateTime end = endDate != null
                ? LocalDateTime.parse(endDate + "T23:59:59", DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : LocalDateTime.now();

        List<Payment> payments = paymentRepository.findByDateRange(start, end);

        PaymentStatsDTO stats = new PaymentStatsDTO();
        stats.setTotalTransactions(payments.size());
        stats.setTotalRevenue(BigDecimal.ZERO);
        stats.setTotalRefunded(BigDecimal.ZERO);
        stats.setSuccessfulTransactions(0);
        stats.setFailedTransactions(0);
        stats.setRefundedTransactions(0);
        stats.setRevenueByPaymentMethod(new HashMap<>());
        stats.setTransactionsByStatus(new HashMap<>());

        for (Payment payment : payments) {
            // Update total revenue
            if ("COMPLETED".equals(payment.getStatus())) {
                stats.setTotalRevenue(stats.getTotalRevenue().add(payment.getAmount()));
                stats.setSuccessfulTransactions(stats.getSuccessfulTransactions() + 1);
            } else if ("REFUNDED".equals(payment.getStatus())) {
                stats.setTotalRefunded(stats.getTotalRefunded().add(payment.getAmount()));
                stats.setRefundedTransactions(stats.getRefundedTransactions() + 1);
            } else if ("FAILED".equals(payment.getStatus())) {
                stats.setFailedTransactions(stats.getFailedTransactions() + 1);
            }

            // Update revenue by payment method
            String method = payment.getPaymentMethod();
            if ("COMPLETED".equals(payment.getStatus())) {
                stats.getRevenueByPaymentMethod().merge(method, payment.getAmount(), BigDecimal::add);
            }

            // Update transactions by status
            stats.getTransactionsByStatus().merge(payment.getStatus(), 1L, Long::sum);
        }

        return stats;
    }

    private PaymentDTO convertToDTO(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setUserId(payment.getUser().getId());
        dto.setUsername(payment.getUser().getUsername());
        dto.setAmount(payment.getAmount());
        dto.setCurrency(payment.getCurrency());
        dto.setStatus(payment.getStatus());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentGatewayId(payment.getPaymentGatewayId());
        dto.setSubscriptionId(payment.getSubscriptionId());
        dto.setDescription(payment.getDescription());
        dto.setErrorMessage(payment.getErrorMessage());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setUpdatedAt(payment.getUpdatedAt());
        dto.setCompletedAt(payment.getCompletedAt());
        return dto;
    }
}