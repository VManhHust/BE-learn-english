package com.example.belearnenglish.dto;

import java.util.List;

public record TopicDto(
        Long id,
        String name,
        String slug,
        String description,
        String thumbnail,
        long lessonCount,
        List<LessonPreviewDto> previewLessons
) {}
