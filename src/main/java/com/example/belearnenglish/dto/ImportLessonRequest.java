package com.example.belearnenglish.dto;

public record ImportLessonRequest(
    Long topicId,
    String title,
    String youtubeUrl,
    String level,
    Boolean hasDictation,
    Boolean hasShadowing
) {}
