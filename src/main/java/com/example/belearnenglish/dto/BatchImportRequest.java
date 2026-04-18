package com.example.belearnenglish.dto;

import java.util.List;

public record BatchImportRequest(
    Long topicId,
    List<LessonImportItem> lessons
) {
    public record LessonImportItem(
        String title,
        String youtubeUrl,
        String level,
        Boolean hasDictation,
        Boolean hasShadowing
    ) {}
}
