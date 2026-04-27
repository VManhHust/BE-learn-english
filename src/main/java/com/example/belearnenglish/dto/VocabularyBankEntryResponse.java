package com.example.belearnenglish.dto;

import java.time.Instant;

public record VocabularyBankEntryResponse(
    Long id,
    String word,
    Instant addedAt
) {}
