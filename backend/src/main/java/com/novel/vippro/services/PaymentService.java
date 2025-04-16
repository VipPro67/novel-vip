package com.novel.vippro.services;

import com.novel.vippro.dto.PaymentDTO;
import com.novel.vippro.dto.PaymentCreateDTO;
import com.novel.vippro.dto.PaymentStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PaymentService {
    PaymentDTO createPayment(PaymentCreateDTO paymentDTO);

    Page<PaymentDTO> getUserPayments(Pageable pageable);

    PaymentDTO getPayment(UUID id);

    void handleWebhook(String payload, String signature);

    void cancelPayment(UUID id);

    PaymentStatsDTO getPaymentStats(String startDate, String endDate);
}