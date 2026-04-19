package com.example.belearnenglish.dto;

public record LeaderboardEntryDto(
        int rank,
        String displayName,
        String avatarUrl,
        int totalScore
) {}
