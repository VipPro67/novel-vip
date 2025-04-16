package com.novel.vippro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentMethodUpdateDTO {
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotNull(message = "Payment details are required")
    private PaymentCreateDTO paymentDetails;
}