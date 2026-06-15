package com.example.belearnenglish.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ProPlanTests {

    private static final Instant NOW = Instant.parse("2026-06-14T10:00:00Z");

    @Test
    void exposesConfiguredPrices() {
        assertThat(ProPlan.MONTHLY.getAmount()).isEqualTo(69_000L);
        assertThat(ProPlan.QUARTERLY.getAmount()).isEqualTo(169_000L);
        assertThat(ProPlan.YEARLY.getAmount()).isEqualTo(499_000L);
        assertThat(ProPlan.LIFETIME.getAmount()).isEqualTo(1_849_000L);
    }

    @Test
    void extendsAnActiveSubscriptionFromItsCurrentExpiry() {
        Instant currentExpiry = NOW.plus(10, ChronoUnit.DAYS);

        assertThat(ProPlan.MONTHLY.calculateExpiry(currentExpiry, NOW))
                .isEqualTo(currentExpiry.plus(30, ChronoUnit.DAYS));
    }

    @Test
    void lifetimeCannotBeShortenedByAnotherPlan() {
        Instant lifetimeExpiry = ProPlan.LIFETIME.calculateExpiry(null, NOW);

        assertThat(ProPlan.MONTHLY.calculateExpiry(lifetimeExpiry, NOW)).isEqualTo(lifetimeExpiry);
    }
}
