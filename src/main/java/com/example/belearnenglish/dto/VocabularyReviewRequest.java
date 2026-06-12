package com.example.belearnenglish.dto;

import jakarta.validation.constraints.NotBlank;

public record VocabularyReviewRequest(
    @NotBlank(message = "Rating is required")
    String rating
) {
}
