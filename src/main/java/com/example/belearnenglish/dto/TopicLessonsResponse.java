package com.example.belearnenglish.dto;

import java.util.List;

public record TopicLessonsResponse(
        Long topicId,
        String topicName,
        long totalElements,
        int totalPages,
        int page,
        int size,
        List<LessonPreviewDto> content
) {}
