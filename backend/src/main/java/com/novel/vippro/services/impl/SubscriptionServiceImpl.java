package com.novel.vippro.services.impl;

import com.novel.vippro.dto.*;
import com.novel.vippro.models.Subscription;
import com.novel.vippro.models.SubscriptionPlan;
import com.novel.vippro.models.User;
import com.novel.vippro.payload.response.PageResponse;
import com.novel.vippro.repository.SubscriptionRepository;
import com.novel.vippro.repository.SubscriptionPlanRepository;
import com.novel.vippro.repository.UserRepository;
import com.novel.vippro.services.SubscriptionService;
import com.novel.vippro.services.PaymentService;
import com.novel.vippro.services.UserService;
import com.novel.vippro.exception.ResourceNotFoundException;
import com.novel.vippro.mapper.Mapper;
import com.novel.vippro.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final PaymentService paymentService;
    private final Mapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanDTO> getSubscriptionPlans() {
        return subscriptionPlanRepository.findByActiveTrue()
                .stream()
                .map(mapper::SubscriptionPlanToSubscriptionPlanDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDTO getCurrentSubscription() {
        UUID userId = userService.getCurrentUserId();
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "userId", userId));
        return mapper.SubscriptionToSubscriptionDTO(subscription);
    }

    @Override
    @Transactional
    public SubscriptionDTO subscribe(SubscriptionCreateDTO subscriptionDTO) {
        UUID userId = userService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if user already has an active subscription
        subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .ifPresent(s -> {
                    throw new BadRequestException("User already has an active subscription");
                });

        SubscriptionPlan plan = subscriptionPlanRepository.findByPlanId(subscriptionDTO.getPlanId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("SubscriptionPlan", "planId", subscriptionDTO.getPlanId()));

        // Create payment
        PaymentCreateDTO paymentDTO = subscriptionDTO.getPaymentDetails();
        paymentDTO.setSubscriptionId(null); // Will be set after subscription creation
        PaymentDTO payment = paymentService.createPayment(paymentDTO);

        // Create subscription
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanId(plan.getPlanId());
        subscription.setStatus("ACTIVE");
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(calculateEndDate(plan.getBillingPeriod()));
        subscription.setPaymentMethod(subscriptionDTO.getPaymentMethod());
        subscription.setPaymentGatewayId(payment.getPaymentGatewayId());

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // Update payment with subscription ID
        paymentDTO.setSubscriptionId(savedSubscription.getId());
        paymentService.createPayment(paymentDTO);

        return mapper.SubscriptionToSubscriptionDTO(savedSubscription);
    }

    @Override
    @Transactional
    public SubscriptionDTO cancelSubscription() {
        UUID userId = userService.getCurrentUserId();
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "userId", userId));

        subscription.setStatus("CANCELLED");
        subscription.setEndDate(LocalDateTime.now());
        return mapper.SubscriptionToSubscriptionDTO(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SubscriptionHistoryDTO> getSubscriptionHistory(Pageable pageable) {
        UUID userId = userService.getCurrentUserId();
        return new PageResponse<>(subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(mapper::SubscriptionToSubscriptionHistoryDTO));
    }

    @Override
    @Transactional
    public SubscriptionDTO updatePaymentMethod(PaymentMethodUpdateDTO paymentMethodDTO) {
        UUID userId = userService.getCurrentUserId();
        Subscription subscription = subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "userId", userId));

        // Create new payment
        PaymentCreateDTO paymentDTO = paymentMethodDTO.getPaymentDetails();
        paymentDTO.setSubscriptionId(subscription.getId());
        PaymentDTO payment = paymentService.createPayment(paymentDTO);

        subscription.setPaymentMethod(paymentMethodDTO.getPaymentMethod());
        subscription.setPaymentGatewayId(payment.getPaymentGatewayId());

        return mapper.SubscriptionToSubscriptionDTO(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasFeatureAccess(String featureKey) {
        UUID userId = userService.getCurrentUserId();
        return subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .map(subscription -> {
                    SubscriptionPlan plan = subscriptionPlanRepository.findByPlanId(subscription.getPlanId())
                            .orElse(null);
                    return plan != null && plan.isActive() && plan.getFeatureList().contains(featureKey);
                })
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureDTO> getAvailableFeatures() {
        UUID userId = userService.getCurrentUserId();
        return subscriptionRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .map(subscription -> {
                    SubscriptionPlan plan = subscriptionPlanRepository.findByPlanId(subscription.getPlanId())
                            .orElse(null);
                    if (plan != null && plan.isActive()) {
                        String[] features = plan.getFeatureList().split(",");
                        List<FeatureDTO> featureDTOs = Arrays.stream(features)
                                .map(feature -> {
                                    FeatureDTO dto = new FeatureDTO();
                                    dto.setKey(feature);
                                    dto.setEnabled(true);
                                    return dto;
                                })
                                .collect(Collectors.toList());
                        return featureDTOs;
                    }
                    return List.<FeatureDTO>of();
                })
                .orElse(List.<FeatureDTO>of());
    }

    private LocalDateTime calculateEndDate(String billingPeriod) {
        LocalDateTime now = LocalDateTime.now();
        return switch (billingPeriod.toUpperCase()) {
            case "MONTHLY" -> now.plusMonths(1);
            case "YEARLY" -> now.plusYears(1);
            default -> throw new BadRequestException("Invalid billing period: " + billingPeriod);
        };
    }
}