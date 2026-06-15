package com.example.belearnenglish.service;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class SepayWebhookVerifierTests {

    private static final Instant NOW = Instant.parse("2026-06-14T10:00:00Z");
    private static final String SECRET = "test-webhook-secret";
    private static final String BODY = "{\"id\":92704,\"transferAmount\":499000}";

    private final SepayWebhookVerifier verifier =
            new SepayWebhookVerifier(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void acceptsValidSignature() throws Exception {
        String timestamp = String.valueOf(NOW.getEpochSecond());

        assertThat(verifier.isValid(BODY, sign(timestamp, BODY), timestamp, SECRET)).isTrue();
    }

    @Test
    void rejectsModifiedPayload() throws Exception {
        String timestamp = String.valueOf(NOW.getEpochSecond());
        String signature = sign(timestamp, BODY);

        assertThat(verifier.isValid(BODY + " ", signature, timestamp, SECRET)).isFalse();
    }

    @Test
    void rejectsExpiredTimestamp() throws Exception {
        String timestamp = String.valueOf(NOW.minusSeconds(301).getEpochSecond());

        assertThat(verifier.isValid(BODY, sign(timestamp, BODY), timestamp, SECRET)).isFalse();
    }

    private String sign(String timestamp, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(hash);
    }
}
