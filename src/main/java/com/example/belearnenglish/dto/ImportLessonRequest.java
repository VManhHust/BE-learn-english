package com.example.belearnenglish.dto;

public record ImportLessonRequest(
        Long topicId,
        String youtubeUrl,
        String title,
        String level,
        String channelYoutubeId
) {}
