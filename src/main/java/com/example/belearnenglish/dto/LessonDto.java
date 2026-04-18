package com.example.belearnenglish.dto;

public record LessonDto(
    Long id,
    String title,
    String thumbnail,
    String duration,
    String level,
    Long viewCount,
    String source,
    Boolean hasDictation,
    Boolean hasShadowing,
    String youtubeUrl,
    String youtubeId
) {}
