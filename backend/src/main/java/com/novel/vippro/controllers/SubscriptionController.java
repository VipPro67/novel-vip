package com.novel.vippro.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novel.vippro.payload.response.ControllerResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import com.novel.vippro.dto.*;
import com.novel.vippro.services.SubscriptionService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/subscriptions")
@Tag(name = "Subscriptions", description = "User subscription and premium features management APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class SubscriptionController {

        private final SubscriptionService subscriptionService;

        @Operation(summary = "Get subscription plans", description = "Get all available subscription plans")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscription plans retrieved successfully")
        })
        @GetMapping("/plans")
        public ResponseEntity<ControllerResponse<List<SubscriptionPlanDTO>>> getSubscriptionPlans() {
                List<SubscriptionPlanDTO> plans = subscriptionService.getSubscriptionPlans();
                return ResponseEntity
                                .ok(ControllerResponse.success("Subscription plans retrieved successfully", plans));
        }

        @Operation(summary = "Get current subscription", description = "Get details of the current user's subscription")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscription details retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "No active subscription found")
        })
        @GetMapping("/current")
        public ResponseEntity<ControllerResponse<SubscriptionDTO>> getCurrentSubscription() {
                SubscriptionDTO subscription = subscriptionService.getCurrentSubscription();
                return ResponseEntity
                                .ok(ControllerResponse.success("Current subscription retrieved successfully",
                                                subscription));
        }

        @Operation(summary = "Subscribe to plan", description = "Subscribe to a specific subscription plan")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscription created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid subscription data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "Plan not found")
        })
        @PostMapping("/subscribe")
        public ResponseEntity<ControllerResponse<SubscriptionDTO>> subscribe(
                        @Parameter(description = "Subscription details", required = true) @Valid @RequestBody SubscriptionCreateDTO subscriptionDTO) {
                SubscriptionDTO subscription = subscriptionService.subscribe(subscriptionDTO);
                return ResponseEntity.ok(ControllerResponse.success("Subscription created successfully", subscription));
        }

        @Operation(summary = "Cancel subscription", description = "Cancel the current user's subscription")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscription cancelled successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "No active subscription found")
        })
        @PostMapping("/cancel")
        public ResponseEntity<ControllerResponse<SubscriptionDTO>> cancelSubscription() {
                SubscriptionDTO subscription = subscriptionService.cancelSubscription();
                return ResponseEntity
                                .ok(ControllerResponse.success("Subscription cancelled successfully", subscription));
        }

        @Operation(summary = "Get subscription history", description = "Get user's subscription history")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Subscription history retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping("/history")
        public ResponseEntity<ControllerResponse<Page<SubscriptionHistoryDTO>>> getSubscriptionHistory(
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                Page<SubscriptionHistoryDTO> history = subscriptionService.getSubscriptionHistory(pageable);
                return ResponseEntity
                                .ok(ControllerResponse.success("Subscription history retrieved successfully", history));
        }

        @Operation(summary = "Update payment method", description = "Update the payment method for current subscription")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment method updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid payment method data"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "No active subscription found")
        })
        @PutMapping("/payment-method")
        public ResponseEntity<ControllerResponse<SubscriptionDTO>> updatePaymentMethod(
                        @Parameter(description = "Payment method details", required = true) @Valid @RequestBody PaymentMethodUpdateDTO paymentMethodDTO) {
                SubscriptionDTO subscription = subscriptionService.updatePaymentMethod(paymentMethodDTO);
                return ResponseEntity
                                .ok(ControllerResponse.success("Payment method updated successfully", subscription));
        }

        @Operation(summary = "Check feature access", description = "Check if user has access to a specific premium feature")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Access status retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping("/features/{featureKey}")
        public ResponseEntity<ControllerResponse<Boolean>> hasFeatureAccess(
                        @Parameter(description = "Feature key", required = true) @PathVariable String featureKey) {
                boolean hasAccess = subscriptionService.hasFeatureAccess(featureKey);
                return ResponseEntity.ok(ControllerResponse.success("Feature access status retrieved", hasAccess));
        }

        @Operation(summary = "Get available features", description = "Get all features available in the user's current subscription")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Features retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @GetMapping("/features")
        public ResponseEntity<ControllerResponse<List<FeatureDTO>>> getAvailableFeatures() {
                List<FeatureDTO> features = subscriptionService.getAvailableFeatures();
                return ResponseEntity
                                .ok(ControllerResponse.success("Available features retrieved successfully", features));
        }
}