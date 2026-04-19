package com.example.belearnenglish.dto;

public record VocabularyStatsDto(
        int totalCards,
        int dueCards,
        int totalReviews,
        double accuracy
) {}
