package com.novel.vippro.services;

import com.novel.vippro.dto.*;
import com.novel.vippro.payload.response.PageResponse;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SubscriptionService {
    List<SubscriptionPlanDTO> getSubscriptionPlans();

    SubscriptionDTO getCurrentSubscription();

    SubscriptionDTO subscribe(SubscriptionCreateDTO subscriptionDTO);

    SubscriptionDTO cancelSubscription();

    PageResponse<SubscriptionHistoryDTO> getSubscriptionHistory(Pageable pageable);

    SubscriptionDTO updatePaymentMethod(PaymentMethodUpdateDTO paymentMethodDTO);

    boolean hasFeatureAccess(String featureKey);

    List<FeatureDTO> getAvailableFeatures();
}