package com.example.belearnenglish.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public final class AdminVocabularyDtos {

    private AdminVocabularyDtos() {
    }

    public record Summary(long decks, long topics, long words, long premiumDecks) {
    }

    public record DeckResponse(
            Long id, String slug, String title, String category, String description,
            String coverColor, String status, boolean premium, int learnerCount,
            int sortOrder, int topicCount, int wordCount,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    }

    public record TopicResponse(
            Long id, Long deckId, String deckTitle, String slug, String title,
            String description, String thumbnailUrl, int sortOrder, int wordCount,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    }

    public record WordResponse(
            Long id, Long topicId, String topicTitle, Long deckId, String deckTitle,
            String word, String partOfSpeech, String ipaUs, String ipaUk,
            String audioUsUrl, String audioUkUrl, String englishDefinition,
            String vietnameseDefinition, String vietnameseTranslation,
            String exampleSentence, String exampleSentenceVi, String imageUrl,
            int sortOrder, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
    }

    public record DeckRequest(
            @NotBlank @Size(max = 120) @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug must use lowercase letters, numbers and hyphens") String slug,
            @NotBlank @Size(max = 255) String title,
            @NotBlank @Size(max = 120) String category,
            String description,
            @NotBlank @Size(max = 30) String coverColor,
            @NotBlank @Pattern(regexp = "DRAFT|PUBLISHED|ARCHIVED", message = "status must be DRAFT, PUBLISHED or ARCHIVED") String status,
            boolean premium,
            @Min(0) int learnerCount,
            @Min(0) int sortOrder) {
    }

    public record TopicRequest(
            @NotNull Long deckId,
            @NotBlank @Size(max = 120) @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug must use lowercase letters, numbers and hyphens") String slug,
            @NotBlank @Size(max = 255) String title,
            String description,
            String thumbnailUrl,
            @Min(0) int sortOrder) {
    }

    public record WordRequest(
            @NotNull Long topicId,
            @NotBlank @Size(max = 160) String word,
            @NotBlank @Size(max = 80) String partOfSpeech,
            @Size(max = 120) String ipaUs,
            @Size(max = 120) String ipaUk,
            String audioUsUrl,
            String audioUkUrl,
            @NotBlank String englishDefinition,
            @NotBlank String vietnameseDefinition,
            @NotBlank @Size(max = 255) String vietnameseTranslation,
            String exampleSentence,
            String exampleSentenceVi,
            String imageUrl,
            @Min(0) int sortOrder) {
    }
}
