package com.example.belearnenglish.dto;

public record VocabularyReviewTopicResponse(
    Long id,
    String slug,
    String title,
    String deckTitle,
    int reviewWordCount
) {}
