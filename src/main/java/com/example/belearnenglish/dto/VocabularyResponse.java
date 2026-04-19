package com.example.belearnenglish.dto;

import java.util.List;

public record VocabularyResponse(
        VocabularyStatsDto stats,
        List<LeaderboardEntryDto> leaderboard
) {}
