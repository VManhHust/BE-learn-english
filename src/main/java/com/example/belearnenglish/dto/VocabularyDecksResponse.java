package com.example.belearnenglish.dto;

import java.util.List;

public record VocabularyDecksResponse(
    int totalDecks,
    List<VocabularyDeckCategoryDto> categories
) {
    public record VocabularyDeckCategoryDto(
        String name,
        int deckCount,
        List<VocabularyDeckCardDto> decks
    ) {
    }

    public record VocabularyDeckCardDto(
        Long id,
        String slug,
        String title,
        String category,
        String description,
        String coverColor,
        boolean premium,
        int topicCount,
        int wordCount,
        int learnerCount,
        int learnedWords,
        int completionPercentage,
        String statusLabel
    ) {
    }
}
