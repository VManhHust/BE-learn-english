package com.example.belearnenglish.dto;

public record UserStatsDto(
        int sessionCount,
        int talkMinutes,
        int likes,
        String displayName
) {}
