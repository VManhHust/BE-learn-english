package com.example.belearnenglish.controller;

import com.example.belearnenglish.dto.CreateProOrderRequest;
import com.example.belearnenglish.dto.PaymentOrderResponse;
import com.example.belearnenglish.dto.ProStatusResponse;
import com.example.belearnenglish.security.JwtClaims;
import com.example.belearnenglish.service.PaymentService;
import com.example.belearnenglish.service.SepayWebhookVerifier;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.List;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SepayWebhookVerifier webhookVerifier;

    @Value("${payment.sepay.webhook-secret:}")
    private String webhookSecret;

    @PostMapping("/pro/orders")
    public ResponseEntity<PaymentOrderResponse> createProOrder(
            @AuthenticationPrincipal JwtClaims claims,
            @Valid @RequestBody CreateProOrderRequest request) {
        return ResponseEntity.ok(paymentService.createProOrder(claims.getUserId(), request.getPlanCode()));
    }

    @GetMapping("/pro/plans")
    public ResponseEntity<List<PaymentService.ProPlanResponse>> getProPlans() {
        return ResponseEntity.ok(paymentService.getProPlans());
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentOrderResponse> getOrder(
            @AuthenticationPrincipal JwtClaims claims,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getOrder(claims.getUserId(), orderId));
    }

    @GetMapping("/pro/status")
    public ResponseEntity<ProStatusResponse> getProStatus(
            @AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(paymentService.getProStatus(claims.getUserId()));
    }

    @PostMapping("/sepay/webhook")
    public ResponseEntity<Map<String, Boolean>> receiveSepayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(name = "X-SePay-Signature", required = false) String signature,
            @RequestHeader(name = "X-SePay-Timestamp", required = false) String timestamp) {
        if (!webhookVerifier.isValid(rawPayload, signature, timestamp, webhookSecret)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid SePay signature");
        }
        paymentService.processSepayWebhook(rawPayload);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
