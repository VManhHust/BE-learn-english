package com.example.belearnenglish.dto;

import com.example.belearnenglish.entity.PaymentOrderStatus;
import com.example.belearnenglish.entity.ProPlan;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PaymentOrderResponse {
    private UUID orderId;
    private ProPlan planCode;
    private String planName;
    private Long amount;
    private String currency;
    private PaymentOrderStatus status;
    private String paymentCode;
    private String qrCodeUrl;
    private String bank;
    private String accountNumber;
    private String accountHolder;
    private Instant expiresAt;
    private Instant paidAt;
    private Instant proExpiresAt;
}
