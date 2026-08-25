package com.example.belearnenglish.service;

import com.example.belearnenglish.dto.SepayWebhookPayload;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentServiceTests {

    @Test
    void matchesConfiguredVirtualAccount() {
        SepayWebhookPayload payload = payload("8804865573", "9624769");

        assertThat(PaymentService.matchesConfiguredAccount("9624769", payload)).isTrue();
    }

    @Test
    void matchesConfiguredPrimaryAccount() {
        SepayWebhookPayload payload = payload("8804865573", "9624769");

        assertThat(PaymentService.matchesConfiguredAccount("8804865573", payload)).isTrue();
    }

    @Test
    void rejectsUnrelatedAccount() {
        SepayWebhookPayload payload = payload("8804865573", "9624769");

        assertThat(PaymentService.matchesConfiguredAccount("123456789", payload)).isFalse();
    }

    private SepayWebhookPayload payload(String accountNumber, String subAccount) {
        SepayWebhookPayload payload = new SepayWebhookPayload();
        payload.setAccountNumber(accountNumber);
        payload.setSubAccount(subAccount);
        return payload;
    }
}
