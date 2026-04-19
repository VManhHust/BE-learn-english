package com.example.belearnenglish.dto;

public record SpeakingResponse(
        UserStatsDto userStats,
        int onlineCount
) {}
