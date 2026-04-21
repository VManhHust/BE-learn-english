package com.example.belearnenglish.dto;

import java.util.List;

public record BatchImportRequest(
        Long topicId,
        String channelYoutubeId,
        List<SaveExerciseRequest> lessons
) {}
