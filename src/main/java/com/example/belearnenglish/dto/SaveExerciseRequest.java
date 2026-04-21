package com.example.belearnenglish.dto;

public record SaveExerciseRequest(
        String videoId,
        String title,
        String thumbnailUrl,
        Integer durationSeconds,
        String vocabularyLevel
) {}
