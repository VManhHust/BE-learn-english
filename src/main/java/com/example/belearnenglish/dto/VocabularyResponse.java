package com.example.belearnenglish.dto;

public record VocabularyResponse(
        int totalWords,
        int learned,
        int reviewing,
        double accuracy
) {}
