package com.novel.vippro.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novel.vippro.payload.response.ControllerResponse;
import com.novel.vippro.payload.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.novel.vippro.dto.PaymentDTO;
import com.novel.vippro.dto.PaymentCreateDTO;
import com.novel.vippro.dto.PaymentStatsDTO;
import com.novel.vippro.services.PaymentService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

        @Autowired
        private PaymentService paymentService;

        @Operation(summary = "Create payment", description = "Initiate a new payment transaction")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment initiated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid payment details"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @PostMapping
        @PreAuthorize("isAuthenticated()")
        public ControllerResponse<PaymentDTO> createPayment(
                        @Parameter(description = "Payment details", required = true) @Valid @RequestBody PaymentCreateDTO paymentDTO) {
                PaymentDTO payment = paymentService.createPayment(paymentDTO);
                return ControllerResponse.success("Payment initiated successfully", payment);
        }

        @Operation(summary = "Get user payments", description = "Get all payments for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/user")
        @PreAuthorize("isAuthenticated()")
        public ControllerResponse<PageResponse<PaymentDTO>> getUserPayments(
                        @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Items per page", example = "10") @RequestParam(defaultValue = "10") int size) {
                Pageable pageable = PageRequest.of(page, size);
                PageResponse<PaymentDTO> payments = paymentService.getUserPayments(pageable);
                return ControllerResponse.success("Payments retrieved successfully", payments);
        }

        @Operation(summary = "Get payment by ID", description = "Get details of a specific payment")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment found"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "Payment not found")
        })
        @GetMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ControllerResponse<PaymentDTO> getPayment(
                        @Parameter(description = "Payment ID", required = true) @PathVariable UUID id) {
                PaymentDTO payment = paymentService.getPayment(id);
                return ControllerResponse.success("Payment retrieved successfully", payment);
        }

        @Operation(summary = "Process payment webhook", description = "Handle payment gateway webhook notifications")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid webhook payload")
        })
        @PostMapping("/webhook")
        public ControllerResponse<Void> handleWebhook(
                        @Parameter(description = "Webhook payload", required = true) @RequestBody String payload,
                        @Parameter(description = "Webhook signature") @RequestHeader(required = false) String signature) {
                paymentService.handleWebhook(payload, signature);
                return ControllerResponse.success("Webhook processed successfully", null);
        }

        @Operation(summary = "Cancel payment", description = "Cancel a pending payment")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Payment cancelled successfully"),
                        @ApiResponse(responseCode = "401", description = "Not authenticated"),
                        @ApiResponse(responseCode = "403", description = "Not authorized"),
                        @ApiResponse(responseCode = "404", description = "Payment not found"),
                        @ApiResponse(responseCode = "400", description = "Payment cannot be cancelled")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("isAuthenticated()")
        public ControllerResponse<Void> cancelPayment(
                        @Parameter(description = "Payment ID", required = true) @PathVariable UUID id) {
                paymentService.cancelPayment(id);
                return ControllerResponse.success("Payment cancelled successfully", null);
        }

        @Operation(summary = "Get payment statistics", description = "Get payment statistics for admin dashboard", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                        @ApiResponse(responseCode = "403", description = "Not authorized")
        })
        @GetMapping("/stats")
        @PreAuthorize("hasRole('ADMIN')")
        public ControllerResponse<PaymentStatsDTO> getPaymentStats(
                        @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) String startDate,
                        @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) String endDate) {
                PaymentStatsDTO stats = paymentService.getPaymentStats(startDate, endDate);
                return ControllerResponse.success("Payment statistics retrieved successfully", stats);
        }
}