package com.example.belearnenglish.dto;

import java.util.List;

public record VocabularyDeckDetailResponse(
    DeckDetailDto deck,
    List<TopicProgressDto> topics,
    TopicProgressDto activeTopic,
    WordCardDto currentCard,
    int currentCardNumber,
    int totalCards,
    int totalDeckWords,
    int learnedDeckWords,
    int deckCompletionPercentage
) {
    public record DeckDetailDto(
        Long id,
        String slug,
        String title,
        String category,
        String description,
        String coverColor,
        boolean premium
    ) {
    }

    public record TopicProgressDto(
        Long id,
        String slug,
        String title,
        String description,
        String thumbnailUrl,
        int sortOrder,
        int totalWords,
        int learnedWords,
        int masteredWords,
        int currentWordIndex,
        int completionPercentage,
        boolean completed
    ) {
    }

    public record WordCardDto(
        Long id,
        String word,
        String partOfSpeech,
        String ipaUs,
        String ipaUk,
        String audioUsUrl,
        String audioUkUrl,
        String englishDefinition,
        String vietnameseDefinition,
        String vietnameseTranslation,
        String exampleSentence,
        String exampleSentenceVi,
        String imageUrl,
        int sortOrder,
        String learningStatus
    ) {
    }
}
