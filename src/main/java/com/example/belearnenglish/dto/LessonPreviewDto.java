package com.example.belearnenglish.dto;

public record LessonPreviewDto(
        Long id,
        String title,
        String thumbnail,
        String duration,
        String level,
        long viewCount,
        String source,
        boolean hasDictation,
        boolean hasShadowing,
        String youtubeId,
        String youtubeUrl
) {}
